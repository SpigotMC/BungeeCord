package net.md_5.bungee.connection;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.crypto.SecretKey;

import com.google.gson.Gson;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.*;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.LegacyHandshake;
import net.md_5.bungee.protocol.packet.LegacyPing;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;

@RequiredArgsConstructor
public class InitialHandler extends PacketHandler implements PendingConnection
{

    private final ProxyServer bungee;
    private ChannelWrapper ch;
    @Getter
    private final ListenerInfo listener;
    @Getter
    private Handshake handshake;
    @Getter
    private LoginRequest loginRequest;
    private EncryptionRequest request;
    @Getter
    private final List<PluginMessage> registerMessages = new ArrayList<>();
    private State thisState = State.HANDSHAKE;
    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }
    };
    @Getter
    private boolean onlineMode = BungeeCord.getInstance().config.isOnlineMode();
    @Getter
    private InetSocketAddress virtualHost;
    @Getter
    private UUID uniqueId;
    @Getter
    private UUID offlineId;
    @Getter
    private LoginResult loginProfile;

    private enum State
    {

        HANDSHAKE, STATUS, PING, USERNAME, ENCRYPT, FINISHED;
    }

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.ch = channel;
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        disconnect( ChatColor.RED + Util.exception( t ) );
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        // TODO: Unregister?
        if ( pluginMessage.getTag().equals( "REGISTER" ) )
        {
            Preconditions.checkState( registerMessages.size() < 128, "Too many channels registered" );
            registerMessages.add( pluginMessage );
        }
    }

    @Override
    public void handle(LegacyHandshake legacyHandshake) throws Exception
    {
        ch.getHandle().writeAndFlush( bungee.getTranslation( "outdated_client" ) );
        ch.close();
    }

    @Override
    public void handle(LegacyPing ping) throws Exception
    {
        ServerPing legacy = new ServerPing( new ServerPing.Protocol( bungee.getName() + " " + bungee.getGameVersion(), bungee.getProtocolVersion() ),
                new ServerPing.Players( listener.getMaxPlayers(), bungee.getOnlineCount(), null ), listener.getMotd(), (Favicon) null );
        legacy = bungee.getPluginManager().callEvent( new ProxyPingEvent( this, legacy ) ).getResponse();

        String kickMessage = ChatColor.DARK_BLUE
                + "\00" + 127
                + "\00" + legacy.getVersion().getName()
                + "\00" + legacy.getDescription()
                + "\00" + legacy.getPlayers().getOnline()
                + "\00" + legacy.getPlayers().getMax();

        ch.getHandle().writeAndFlush( kickMessage );
        ch.close();
    }

    @Override
    public void handle(StatusRequest statusRequest) throws Exception
    {
        Preconditions.checkState( thisState == State.STATUS, "Not expecting STATUS" );

        ServerInfo forced = AbstractReconnectHandler.getForcedHost( this );
        final String motd = ( forced != null ) ? forced.getMotd() : listener.getMotd();

        Callback<ServerPing> pingBack = new Callback<ServerPing>()
        {
            @Override
            public void done(ServerPing result, Throwable error)
            {
                if ( error != null )
                {
                    result = new ServerPing();
                    result.setDescription( bungee.getTranslation( "ping_cannot_connect" ) );
                    bungee.getLogger().log( Level.WARNING, "Error pinging remote server", error );
                }
                result = bungee.getPluginManager().callEvent( new ProxyPingEvent( InitialHandler.this, result ) ).getResponse();

                BungeeCord.getInstance().getConnectionThrottle().unthrottle( getAddress().getAddress() );
                Gson gson = handshake.getProtocolVersion() == ProtocolConstants.MINECRAFT_1_7_2 ? BungeeCord.getInstance().gsonLegacy : BungeeCord.getInstance().gson;
                unsafe.sendPacket( new StatusResponse( gson.toJson( result ) ) );
            }
        };

        if ( forced != null && listener.isPingPassthrough() )
        {
            ( (BungeeServerInfo) forced ).ping( pingBack, handshake.getProtocolVersion() );
        } else
        {
            int protocol = ( Protocol.supportedVersions.contains( handshake.getProtocolVersion() ) ) ? handshake.getProtocolVersion() : bungee.getProtocolVersion();
            pingBack.done( new ServerPing(
                    new ServerPing.Protocol( bungee.getName() + " " + bungee.getGameVersion(), protocol ),
                    new ServerPing.Players( listener.getMaxPlayers(), bungee.getOnlineCount(), null ),
                    motd, BungeeCord.getInstance().config.getFaviconObject() ),
                    null );
        }

        thisState = State.PING;
    }

    @Override
    public void handle(PingPacket ping) throws Exception
    {
        Preconditions.checkState( thisState == State.PING, "Not expecting PING" );
        unsafe.sendPacket( ping );
        disconnect( "" );
    }

    @Override
    public void handle(Handshake handshake) throws Exception
    {
        Preconditions.checkState( thisState == State.HANDSHAKE, "Not expecting HANDSHAKE" );
        this.handshake = handshake;
        ch.setVersion( handshake.getProtocolVersion() );

        // SRV records can end with a . depending on DNS / client.
        if ( handshake.getHost().endsWith( "." ) )
        {
            handshake.setHost( handshake.getHost().substring( 0, handshake.getHost().length() - 1 ) );
        }

        this.virtualHost = InetSocketAddress.createUnresolved( handshake.getHost(), handshake.getPort() );
        bungee.getLogger().log( Level.INFO, "{0} has connected", this );

        bungee.getPluginManager().callEvent( new PlayerHandshakeEvent( InitialHandler.this, handshake ) );

        switch ( handshake.getRequestedProtocol() )
        {
            case 1:
                // Ping
                thisState = State.STATUS;
                ch.setProtocol( Protocol.STATUS );
                break;
            case 2:
                thisState = State.USERNAME;
                ch.setProtocol( Protocol.LOGIN );
                // Login
                break;
            default:
                throw new IllegalArgumentException( "Cannot request protocol " + handshake.getRequestedProtocol() );
        }
    }

    @Override
    public void handle(LoginRequest loginRequest) throws Exception
    {
        Preconditions.checkState( thisState == State.USERNAME, "Not expecting USERNAME" );
        this.loginRequest = loginRequest;

        if ( !Protocol.supportedVersions.contains( handshake.getProtocolVersion() ) )
        {
            disconnect( bungee.getTranslation( "outdated_server" ) );
            return;
        }

        if ( getName().length() > 16 )
        {
            disconnect( bungee.getTranslation( "name_too_long" ) );
            return;
        }

        int limit = BungeeCord.getInstance().config.getPlayerLimit();
        if ( limit > 0 && bungee.getOnlineCount() > limit )
        {
            disconnect( bungee.getTranslation( "proxy_full" ) );
            return;
        }

        // If offline mode and they are already on, don't allow connect
        if ( !isOnlineMode() && bungee.getPlayer( getName() ) != null )
        {
            disconnect( bungee.getTranslation( "already_connected" ) );
            return;
        }

        // TODO: Nuuuu Mojang why u do this
        // unsafe().sendPacket( PacketConstants.I_AM_BUNGEE );
        // unsafe().sendPacket( PacketConstants.FORGE_MOD_REQUEST );
        Callback<PreLoginEvent> callback = new Callback<PreLoginEvent>()
        {

            @Override
            public void done(PreLoginEvent result, Throwable error)
            {
                if ( result.isCancelled() )
                {
                    disconnect( result.getCancelReason() );
                    return;
                }
                if ( ch.isClosed() )
                {
                    return;
                }
                if ( onlineMode )
                {
                    unsafe().sendPacket( request = EncryptionUtil.encryptRequest() );
                } else
                {
                    finish();
                }
                thisState = State.ENCRYPT;
            }
        };

        // fire pre login event
        bungee.getPluginManager().callEvent( new PreLoginEvent( InitialHandler.this, callback ) );
    }

    @Override
    public void handle(final EncryptionResponse encryptResponse) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT, "Not expecting ENCRYPT" );

        SecretKey sharedKey = EncryptionUtil.getSecret( encryptResponse, request );
        BungeeCipher decrypt = EncryptionUtil.getCipher( false, sharedKey );
        ch.addBefore( PipelineUtils.FRAME_DECODER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder( decrypt ) );
        BungeeCipher encrypt = EncryptionUtil.getCipher( true, sharedKey );
        ch.addBefore( PipelineUtils.FRAME_PREPENDER, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder( encrypt ) );

        String encName = URLEncoder.encode( InitialHandler.this.getName(), "UTF-8" );

        MessageDigest sha = MessageDigest.getInstance( "SHA-1" );
        for ( byte[] bit : new byte[][]
        {
            request.getServerId().getBytes( "ISO_8859_1" ), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()
        } )
        {
            sha.update( bit );
        }
        String encodedHash = URLEncoder.encode( new BigInteger( sha.digest() ).toString( 16 ), "UTF-8" );

        String authURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encName + "&serverId=" + encodedHash;

        Callback<String> handler = new Callback<String>()
        {
            @Override
            public void done(String result, Throwable error)
            {
                if ( error == null )
                {
                    LoginResult obj = BungeeCord.getInstance().gson.fromJson( result, LoginResult.class );
                    if ( obj != null )
                    {
                        loginProfile = obj;
                        uniqueId = Util.getUUID( obj.getId() );
                        finish();
                        return;
                    }
                    disconnect( "Not authenticated with Minecraft.net" );
                } else
                {
                    disconnect( bungee.getTranslation( "mojang_fail" ) );
                    bungee.getLogger().log( Level.SEVERE, "Error authenticating " + getName() + " with minecraft.net", error );
                }
            }
        };

        HttpClient.get( authURL, ch.getHandle().eventLoop(), handler );
    }

    private void finish()
    {
        // Check for multiple connections
        ProxiedPlayer old = bungee.getPlayer( getName() );
        if ( old != null )
        {
            old.disconnect( bungee.getTranslation( "already_connected" ) );
        }

        offlineId = java.util.UUID.nameUUIDFromBytes( ( "OfflinePlayer:" + getName() ).getBytes( Charsets.UTF_8 ) );
        if ( uniqueId == null )
        {
            uniqueId = offlineId;
        }

        Callback<LoginEvent> complete = new Callback<LoginEvent>()
        {
            @Override
            public void done(LoginEvent result, Throwable error)
            {
                if ( result.isCancelled() )
                {
                    disconnect( result.getCancelReason() );
                    return;
                }
                if ( ch.isClosed() )
                {
                    return;
                }

                ch.getHandle().eventLoop().execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( ch.getHandle().isActive() )
                        {
                            if ( getVersion() >= ProtocolConstants.MINECRAFT_1_7_6 )
                            {
                                unsafe.sendPacket( new LoginSuccess( getUniqueId().toString(), getName() ) ); // With dashes in between
                            } else
                            {
                                unsafe.sendPacket( new LoginSuccess( getUUID(), getName() ) ); // Without dashes, for older clients.
                            }
                            ch.setProtocol( Protocol.GAME );

                            UserConnection userCon = new UserConnection( bungee, ch, getName(), InitialHandler.this );
                            userCon.init();

                            bungee.getPluginManager().callEvent( new PostLoginEvent( userCon ) );

                            ch.getHandle().pipeline().get( HandlerBoss.class ).setHandler( new UpstreamBridge( bungee, userCon ) );

                            ServerInfo server;
                            if ( bungee.getReconnectHandler() != null )
                            {
                                server = bungee.getReconnectHandler().getServer( userCon );
                            } else
                            {
                                server = AbstractReconnectHandler.getForcedHost( InitialHandler.this );
                            }
                            if ( server == null )
                            {
                                server = bungee.getServerInfo( listener.getDefaultServer() );
                            }

                            userCon.connect( server, null, true );

                            thisState = State.FINISHED;
                        }
                    }
                } );
            }
        };

        // fire login event
        bungee.getPluginManager().callEvent( new LoginEvent( InitialHandler.this, complete ) );
    }

    @Override
    public void disconnect(String reason)
    {
        disconnect( TextComponent.fromLegacyText( reason ) );
    }

    @Override
    public void disconnect(final BaseComponent... reason)
    {
        if ( !ch.isClosed() )
        {
            // Why do we have to delay this you might ask? Well the simple reason is MOJANG.
            // Despite many a bug report posted, ever since the 1.7 protocol rewrite, the client STILL has a race condition upon switching protocols.
            // As such, despite the protocol switch packets already having been sent, there is the possibility of a client side exception
            // To help combat this we will wait half a second before actually sending the disconnected packet so that whoever is on the other
            // end has a somewhat better chance of receiving the proper packet.
            ch.getHandle().eventLoop().schedule( new Runnable()
            {

                @Override
                public void run()
                {
                    unsafe().sendPacket( new Kick( ComponentSerializer.toString( reason ) ) );
                    ch.close();
                }
            }, 500, TimeUnit.MILLISECONDS );
        }
    }

    @Override
    public void disconnect(BaseComponent reason)
    {
        disconnect( new BaseComponent[]
        {
            reason
        } );
    }

    @Override
    public String getName()
    {
        return ( loginRequest == null ) ? null : loginRequest.getData();
    }

    @Override
    public int getVersion()
    {
        return ( handshake == null ) ? -1 : handshake.getProtocolVersion();
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) ch.getHandle().remoteAddress();
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }

    @Override
    public void setOnlineMode(boolean onlineMode)
    {
        Preconditions.checkState( thisState == State.USERNAME, "Can only set online mode status whilst state is username" );
        this.onlineMode = onlineMode;
    }

    @Override
    public String getUUID()
    {
        return uniqueId.toString().replaceAll( "-", "" );
    }

    @Override
    public String toString()
    {
        return "[" + ( ( getName() != null ) ? getName() : getAddress() ) + "] <-> InitialHandler";
    }
}
