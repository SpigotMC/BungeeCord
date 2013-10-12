package net.md_5.bungee.connection;

import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.CipherDecoder;
import net.md_5.bungee.netty.CipherEncoder;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.StatusRequest;

@RequiredArgsConstructor
public class InitialHandler extends PacketHandler implements PendingConnection
{

    private final ProxyServer bungee;
    private ChannelWrapper ch;
    @Getter
    private final ListenerInfo listener;
    @Getter
    private Login forgeLogin;
    @Getter
    private Handshake handshake;
    @Getter
    private LoginRequest loginRequest;
    private EncryptionRequest request;
    @Getter
    private List<PluginMessage> loginMessages = new ArrayList<>();
    @Getter
    private List<PluginMessage> registerMessages = new ArrayList<>();
    private State thisState = State.HANDSHAKE;
    private SecretKey sharedKey;
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
    private InetSocketAddress vHost;
    private byte version = -1;
    @Getter
    private String UUID;

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
    public void handle(PacketWrapper packet) throws Exception
    {
        int len = DefinedPacket.readVarInt( packet.buf );
        int id = DefinedPacket.readVarInt( packet.buf );
        // throw new UnsupportedOperationException( "Cannot handle unknown packet at login!" );
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
            registerMessages.add( pluginMessage );
        } else
        {
            loginMessages.add( pluginMessage );
        }
    }

    @Override
    public void handle(StatusRequest statusRequest) throws Exception
    {
        ServerInfo forced = AbstractReconnectHandler.getForcedHost( this );
        final String motd = ( forced != null ) ? forced.getMotd() : listener.getMotd();

        Callback<ServerPing> pingBack = new Callback<ServerPing>()
        {
            @Override
            public void done(ServerPing result, Throwable error)
            {
                if ( error != null )
                {
                    result = new ServerPing( (byte) -1, "-1", "Error pinging remote server: " + Util.exception( error ), -1, -1 );
                }
                result = bungee.getPluginManager().callEvent( new ProxyPingEvent( InitialHandler.this, result ) ).getResponse();

                String kickMessage = ChatColor.DARK_BLUE
                        + "\00" + result.getProtocolVersion()
                        + "\00" + result.getGameVersion()
                        + "\00" + result.getMotd()
                        + "\00" + result.getCurrentPlayers()
                        + "\00" + result.getMaxPlayers();
                BungeeCord.getInstance().getConnectionThrottle().unthrottle( getAddress().getAddress() );
                disconnect( kickMessage );
            }
        };

        if ( forced != null && listener.isPingPassthrough() )
        {
            forced.ping( pingBack );
        } else
        {
            pingBack.done( new ServerPing( bungee.getProtocolVersion(), bungee.getGameVersion(), motd, bungee.getOnlineCount(), listener.getMaxPlayers() ), null );
        }
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
        this.vHost = new InetSocketAddress( handshake.getHost(), handshake.getPort() );
        bungee.getLogger().log( Level.INFO, "{0} has connected", this );

        bungee.getPluginManager().callEvent( new PlayerHandshakeEvent( InitialHandler.this, handshake ) );

        if ( handshake.getProtocolVersion() > bungee.getProtocolVersion() )
        {
            disconnect( bungee.getTranslation( "outdated_server" ) );
        } else if ( handshake.getProtocolVersion() < bungee.getProtocolVersion() )
        {
            disconnect( bungee.getTranslation( "outdated_client" ) );
        }

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

        if ( getName().length() > 16 )
        {
            disconnect( "Cannot have username longer than 16 characters" );
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

        unsafe().sendPacket( request = EncryptionUtil.encryptRequest( this.onlineMode ) );
        thisState = State.ENCRYPT;
    }

    @Override
    public void handle(final EncryptionResponse encryptResponse) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT, "Not expecting ENCRYPT" );

        sharedKey = EncryptionUtil.getSecret( encryptResponse, request );
        Cipher decrypt = EncryptionUtil.getCipher( Cipher.DECRYPT_MODE, sharedKey );
        ch.addBefore( PipelineUtils.FRAME_DECODER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder( decrypt ) );

        if ( this.onlineMode )
        {
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
                            UUID = obj.getId();
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
        } else
        {
            finish();
        }
    }

    private void finish()
    {
        // Check for multiple connections
        ProxiedPlayer old = bungee.getPlayer( getName() );
        if ( old != null )
        {
            old.disconnect( bungee.getTranslation( "already_connected" ) );
        }

        Callback<LoginEvent> complete = new Callback<LoginEvent>()
        {
            @Override
            public void done(LoginEvent result, Throwable error)
            {
                if ( result.isCancelled() )
                {
                    disconnect( result.getCancelReason() );
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

                            if ( onlineMode )
                            {
                                // unsafe().sendPacket( new EncryptionResponse( new byte[ 0 ], new byte[ 0 ] ) );
                                try
                                {
                                    Cipher encrypt = EncryptionUtil.getCipher( Cipher.ENCRYPT_MODE, sharedKey );
                                    ch.addBefore( PipelineUtils.DECRYPT_HANDLER, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder( encrypt ) );
                                } catch ( GeneralSecurityException ex )
                                {
                                    disconnect( "Cipher error: " + Util.exception( ex ) );
                                }
                            }

                            if ( UUID == null )
                            {
                                UUID = java.util.UUID.randomUUID().toString();
                            }
                            System.out.println( ch.getHandle().pipeline() );
                            unsafe.sendPacket( new LoginSuccess( UUID, getName() ) );
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
                            userCon.connect( server, true );

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
    public synchronized void disconnect(String reason)
    {
        if ( !ch.isClosed() )
        {
            unsafe().sendPacket( new Kick( BungeeCord.getInstance().gson.toJson( reason ) ) );
            ch.close();
        }
    }

    @Override
    public String getName()
    {
        return ( loginRequest == null ) ? null : loginRequest.getData();
    }

    @Override
    public int getVersion()
    {
        return ( handshake == null ) ? version : handshake.getProtocolVersion();
    }

    @Override
    public InetSocketAddress getVirtualHost()
    {
        return vHost;
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

    public void setOnlineMode(boolean onlineMode)
    {
        Preconditions.checkState( thisState == State.HANDSHAKE, "Can only set online mode status whilst handshaking" );
        this.onlineMode = onlineMode;
    }

    @Override
    public String toString()
    {
        return "[" + ( ( getName() != null ) ? getName() : getAddress() ) + "] <-> InitialHandler";
    }
}
