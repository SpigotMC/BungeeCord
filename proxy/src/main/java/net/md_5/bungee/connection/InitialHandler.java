package net.md_5.bungee.connection;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.event.PlayerSetUUIDEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LegacyHandshake;
import net.md_5.bungee.protocol.packet.LegacyPing;
import net.md_5.bungee.protocol.packet.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.util.BoundedArrayList;
import net.md_5.bungee.util.QuietException;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.utils.FastException;
import ru.leymooo.botfilter.utils.IPUtils;
import ru.leymooo.botfilter.utils.PingLimiter;

@RequiredArgsConstructor
public class InitialHandler extends PacketHandler implements PendingConnection
{

    private final BungeeCord bungee;
    @Getter //BotFilter
    private ChannelWrapper ch;
    @Getter
    private final ListenerInfo listener;
    @Getter
    private Handshake handshake;
    @Getter
    private LoginRequest loginRequest;
    private EncryptionRequest request;
    @Getter
    private final List<PluginMessage> relayMessages = new BoundedArrayList<>( 128 );
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
    private String name;
    @Getter
    private UUID uniqueId;
    @Getter
    private UUID offlineId;
    @Getter
    private LoginResult loginProfile;
    @Getter
    private boolean legacy;
    @Getter
    private String extraDataInHandshake = "";

    @Override
    public boolean shouldHandle(PacketWrapper packet) throws Exception
    {
        return !ch.isClosing();
    }

    private enum State
    {

        HANDSHAKE, STATUS, PING, USERNAME, ENCRYPT, FINISHED;
    }

    private boolean canSendKickMessage()
    {
        return thisState == State.USERNAME || thisState == State.ENCRYPT || thisState == State.FINISHED;
    }

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.ch = channel;
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        if ( canSendKickMessage() )
        {
            disconnect( ChatColor.RED + Util.exception( t ) );
        } else
        {
            ch.close();
        }
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception
    {
        if ( packet.packet == null && !ch.isClosed() ) //BotFilter ch.isClosed
        {
            throw new QuietException( "Unexpected packet received during login process! " ); //BotFilter removed bump
        }
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        // TODO: Unregister?
        if ( PluginMessage.SHOULD_RELAY.apply( pluginMessage ) )
        {
            relayMessages.add( pluginMessage );
        }
    }

    @Override
    public void handle(LegacyHandshake legacyHandshake) throws Exception
    {
        this.legacy = true;
        ch.close( bungee.getTranslation( "outdated_client", bungee.getGameVersion() ) );
    }

    @Override
    public void handle(LegacyPing ping) throws Exception
    {
        checkState( !this.legacy, "Multiple legacy pings" ); //BotFilter
        this.legacy = true;
        final boolean v1_5 = ping.isV1_5();

        ServerPing legacy = new ServerPing( new ServerPing.Protocol( bungee.getCustomBungeeName(), bungee.getProtocolVersion() ), //BotFilter
                new ServerPing.Players( listener.getMaxPlayers(), bungee.getOnlineCountBF( true ), null ), //BotFilter
                new TextComponent( TextComponent.fromLegacyText( listener.getMotd() ) ), (Favicon) null );

        Callback<ProxyPingEvent> callback = new Callback<ProxyPingEvent>()
        {
            @Override
            public void done(ProxyPingEvent result, Throwable error)
            {
                if ( ch.isClosed() )
                {
                    return;
                }

                ServerPing legacy = result.getResponse();
                String kickMessage;

                if ( v1_5 )
                {
                    kickMessage = ChatColor.DARK_BLUE
                            + "\00" + 127
                            + '\00' + legacy.getVersion().getName()
                            + '\00' + getFirstLine( legacy.getDescription() )
                            + '\00' + legacy.getPlayers().getOnline()
                            + '\00' + legacy.getPlayers().getMax();
                } else
                {
                    // Clients <= 1.3 don't support colored motds because the color char is used as delimiter
                    kickMessage = ChatColor.stripColor( getFirstLine( legacy.getDescription() ) )
                            + '\u00a7' + legacy.getPlayers().getOnline()
                            + '\u00a7' + legacy.getPlayers().getMax();
                }

                ch.close( kickMessage );
            }
        };

        bungee.getPluginManager().callEvent( new ProxyPingEvent( this, legacy, callback ) );
    }

    private static String getFirstLine(String str)
    {
        int pos = str.indexOf( '\n' );
        return pos == -1 ? str : str.substring( 0, pos );
    }

    private ServerPing getPingInfo(String motd, int protocol)
    {
        return new ServerPing(
                new ServerPing.Protocol( bungee.getCustomBungeeName(), protocol ), //BotFilter
                new ServerPing.Players( listener.getMaxPlayers(), bungee.getOnlineCountBF( true ), null ), //BotFilter
                motd, PingLimiter.handle() ? null : BungeeCord.getInstance().config.getFaviconObject() //BotFilter PingLimiter.handle() ? null :
        );
    }

    @Override
    public void handle(StatusRequest statusRequest) throws Exception
    {
        checkState( thisState == State.STATUS, "Not expecting STATUS" ); //BotFilter

        ServerInfo forced = AbstractReconnectHandler.getForcedHost( this );
        final String motd = ( forced != null ) ? forced.getMotd() : listener.getMotd();
        final int protocol = ( ProtocolConstants.SUPPORTED_VERSION_IDS.contains( handshake.getProtocolVersion() ) ) ? handshake.getProtocolVersion() : bungee.getProtocolVersion();

        Callback<ServerPing> pingBack = new Callback<ServerPing>()
        {
            @Override
            public void done(ServerPing result, Throwable error)
            {
                if ( error != null )
                {
                    result = getPingInfo( bungee.getTranslation( "ping_cannot_connect" ), protocol );
                    bungee.getLogger().log( Level.WARNING, "Error pinging remote server", error );
                }

                Callback<ProxyPingEvent> callback = new Callback<ProxyPingEvent>()
                {
                    @Override
                    public void done(ProxyPingEvent pingResult, Throwable error)
                    {
                        Gson gson = BungeeCord.getInstance().gson;
                        unsafe.sendPacket( new StatusResponse( gson.toJson( pingResult.getResponse() ) ) );
                        if ( bungee.getConnectionThrottle() != null )
                        {
                            bungee.getConnectionThrottle().unthrottle( getSocketAddress() );
                        }
                    }
                };

                bungee.getPluginManager().callEvent( new ProxyPingEvent( InitialHandler.this, result, callback ) );
            }
        };

        if ( forced != null && listener.isPingPassthrough() )
        {
            ( (BungeeServerInfo) forced ).ping( pingBack, handshake.getProtocolVersion() );
        } else
        {
            pingBack.done( getPingInfo( motd, protocol ), null );
        }

        thisState = State.PING;
    }

    @Override
    public void handle(PingPacket ping) throws Exception
    {
        checkState( thisState == State.PING, "Not expecting PING" ); //BotFilter
        unsafe.sendPacket( ping );
        disconnect( "" );
    }

    @Override
    public void handle(Handshake handshake) throws Exception
    {
        checkState( thisState == State.HANDSHAKE, "Not expecting HANDSHAKE" ); //BotFilter
        this.handshake = handshake;
        ch.setVersion( handshake.getProtocolVersion() );

        // Starting with FML 1.8, a "\0FML\0" token is appended to the handshake. This interferes
        // with Bungee's IP forwarding, so we detect it, and remove it from the host string, for now.
        // We know FML appends \00FML\00. However, we need to also consider that other systems might
        // add their own data to the end of the string. So, we just take everything from the \0 character
        // and save it for later.
        if ( handshake.getHost().contains( "\0" ) )
        {
            String[] split = handshake.getHost().split( "\0", 2 );
            handshake.setHost( split[0] );
            extraDataInHandshake = "\0" + split[1];
        }

        // SRV records can end with a . depending on DNS / client.
        if ( handshake.getHost().endsWith( "." ) )
        {
            handshake.setHost( handshake.getHost().substring( 0, handshake.getHost().length() - 1 ) );
        }

        this.virtualHost = InetSocketAddress.createUnresolved( handshake.getHost(), handshake.getPort() );

        bungee.getPluginManager().callEvent( new PlayerHandshakeEvent( InitialHandler.this, handshake ) );
        switch ( handshake.getRequestedProtocol() )
        {
            case 1:
                // Ping
                if ( bungee.getConfig().isLogPings() )
                {
                    bungee.getLogger().log( Level.INFO, "{0} has pinged", this );
                }
                thisState = State.STATUS;
                ch.setProtocol( Protocol.STATUS );
                bungee.getBotFilter().getServerPingUtils().add( getAddress().getAddress() ); //BotFilter
                break;
            case 2:
                // Login
                bungee.getLogger().log( Level.INFO, "{0} has connected", this );
                thisState = State.USERNAME;
                ch.setProtocol( Protocol.LOGIN );

                if ( !ProtocolConstants.SUPPORTED_VERSION_IDS.contains( handshake.getProtocolVersion() ) )
                {
                    if ( handshake.getProtocolVersion() > bungee.getProtocolVersion() )
                    {
                        disconnect( bungee.getTranslation( "outdated_server", bungee.getGameVersion() ) );
                    } else
                    {
                        disconnect( bungee.getTranslation( "outdated_client", bungee.getGameVersion() ) );
                    }
                    return;
                }
                break;
            default:
                throw new FastException( "[" + ch.getHandle().remoteAddress() + "] Cannot request protocol " + handshake.getRequestedProtocol() ); //BotFilter
        }
    }

    @Override
    public void handle(LoginRequest loginRequest) throws Exception
    {
        checkState( thisState == State.USERNAME, "Not expecting USERNAME" ); //BotFilter
        this.loginRequest = loginRequest;

        bungee.getBotFilter().checkAsyncIfNeeded( this );
        //BotFilter moved code to delayedHandleOfLoginRequset();
    }

    public void delayedHandleOfLoginRequset()
    {
        if ( getName().contains( "." ) )
        {
            disconnect( bungee.getTranslation( "name_invalid" ) );
            return;
        }

        if ( getName().length() > 16 )
        {
            disconnect( bungee.getTranslation( "name_too_long" ) );
            return;
        }

        int limit = BungeeCord.getInstance().config.getPlayerLimit();
        if ( limit > 0 && bungee.getOnlineCountBF( false ) > limit )//BotFilter
        {
            disconnect( bungee.getTranslation( "proxy_full" ) );
            return;
        }

        // If offline mode and they are already on, don't allow connect
        // We can just check by UUID here as names are based on UUID
        if ( !isOnlineMode() && bungee.getPlayer( getUniqueId() ) != null )
        {
            disconnect( bungee.getTranslation( "already_connected_proxy" ) );
            return;
        }

        Callback<PreLoginEvent> callback = new Callback<PreLoginEvent>()
        {

            @Override
            public void done(PreLoginEvent result, Throwable error)
            {
                if ( result.isCancelled() )
                {
                    disconnect( result.getCancelReasonComponents() );
                    return;
                }
                if ( ch.isClosed() )
                {
                    return;
                }
                if ( onlineMode )
                {
                    unsafe().sendPacket( request = EncryptionUtil.encryptRequest() );
                } else if ( isInEventLoop() )
                {
                    finish();
                } else
                {
                    ch.getHandle().eventLoop().execute( () ->
                    {
                        if ( !ch.isClosing() )
                        {
                            finish();
                        }
                    } );
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
        checkState( thisState == State.ENCRYPT, "Not expecting ENCRYPT" ); //BotFilter

        SecretKey sharedKey = EncryptionUtil.getSecret( encryptResponse, request );
        BungeeCipher decrypt = EncryptionUtil.getCipher( false, sharedKey );
        ch.addBefore( PipelineUtils.FRAME_DECODER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder( decrypt ) );
        BungeeCipher encrypt = EncryptionUtil.getCipher( true, sharedKey );
        ch.addBefore( PipelineUtils.FRAME_PREPENDER, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder( encrypt ) );

        String encName = URLEncoder.encode( InitialHandler.this.getName(), "UTF-8" );

        MessageDigest sha = MessageDigest.getInstance( "SHA-1" );
        sha.update( request.getServerId().getBytes( StandardCharsets.ISO_8859_1 ) );
        sha.update( sharedKey.getEncoded() );
        sha.update( EncryptionUtil.keys.getPublic().getEncoded() );
        String encodedHash = URLEncoder.encode( new BigInteger( sha.digest() ).toString( 16 ), "UTF-8" );

        String preventProxy = ( BungeeCord.getInstance().config.isPreventProxyConnections() && getSocketAddress() instanceof InetSocketAddress ) ? "&ip=" + URLEncoder.encode( getAddress().getAddress().getHostAddress(), "UTF-8" ) : "";
        String authURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encName + "&serverId=" + encodedHash + preventProxy;

        Callback<String> handler = new Callback<String>()
        {
            @Override
            public void done(String result, Throwable error)
            {
                if ( error == null )
                {
                    LoginResult obj = BungeeCord.getInstance().gson.fromJson( result, LoginResult.class );
                    if ( obj != null && obj.getId() != null )
                    {
                        loginProfile = obj;
                        name = obj.getName();
                        uniqueId = Util.getUUID( obj.getId() );
                        finish();
                        return;
                    }
                    disconnect( bungee.getTranslation( "offline_mode_player" ) );
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
        if ( isOnlineMode() )
        {
            // Check for multiple connections
            // We have to check for the old name first
            ProxiedPlayer oldName = bungee.getPlayer( getName() );
            if ( oldName != null )
            {
                // TODO See #1218
                oldName.disconnect( bungee.getTranslation( "already_connected_proxy" ) ); // TODO: Cache this disconnect packet
            }
            // And then also for their old UUID
            ProxiedPlayer oldID = bungee.getPlayer( getUniqueId() );
            if ( oldID != null )
            {
                // TODO See #1218
                oldID.disconnect( bungee.getTranslation( "already_connected_proxy" ) ); // TODO: Cache this disconnect packet
            }
        } else
        {
            // In offline mode the existing user stays and we kick the new one
            ProxiedPlayer oldName = bungee.getPlayer( getName() );
            if ( oldName != null )
            {
                // TODO See #1218
                disconnect( bungee.getTranslation( "already_connected_proxy" ) ); // TODO: Cache this disconnect packet
                return;
            }

        }

        //BotFilter start
        if ( bungee.getBotFilter().isOnChecking( getName() ) )
        {
            disconnect( bungee.getTranslation( "already_connected_proxy" ) ); // TODO: Cache this disconnect packet
            return;
        }

        offlineId = UUID.nameUUIDFromBytes( ( "OfflinePlayer:" + getName() ).getBytes( Charsets.UTF_8 ) );

        PlayerSetUUIDEvent uuidEvent = new PlayerSetUUIDEvent( this, offlineId );
        //Because botfilter delayed a LoginEvent when player needs a check for a bot,
        //plugins can not change a uniqueId field via reflection in event, because BotFilter needs send
        //a LoginSuccess packet before a LoginEvent will be fired.
        bungee.getPluginManager().callEvent( uuidEvent );

        if ( uuidEvent.getUniqueId() != null )
        {
            uniqueId = uuidEvent.getUniqueId();
        }

        boolean sendLoginSuccess = uuidEvent.getUniqueId() != null;

        if ( uniqueId == null )
        {
            uniqueId = offlineId;
        }

        UserConnection userCon = new UserConnection( bungee, ch, getName(), InitialHandler.this );
        userCon.setCompressionThreshold( BungeeCord.getInstance().config.getCompressionThreshold() );
        //userCon.init();

        sendLoginSuccess( sendLoginSuccess );

        if ( Settings.IMP.PROTECTION.ALWAYS_CHECK || bungee.getBotFilter().needCheck( getName(), getAddress().getAddress() ) )
        {
            sendLoginSuccess( !sendLoginSuccess ); //Send a loginSuccess if sendLoginSuccess is false
            bungee.getBotFilter().connectToBotFilter( userCon );
        } else
        {
            bungee.getBotFilter().saveUser( userCon.getName().toLowerCase(), IPUtils.getAddress( userCon ), false ); //update timestamp
            finishLogin( userCon, sendLoginSuccess ); //if true, dont send again login success
        }
    }

    public void finishLogin(UserConnection userCon, boolean ignoreLoginSuccess)
    {

        Callback<LoginEvent> complete = ( result, error ) ->
        {
            if ( result.isCancelled() )
            {
                disconnect( result.getCancelReasonComponents() );
                return;
            }
            if ( ch.isClosed() )
            {
                return;
            }
            if ( isInEventLoop() )
            {
                finnalyFinishLogin( userCon, ignoreLoginSuccess );
            } else
            {
                ch.getHandle().eventLoop().execute( () ->
                {
                    if ( !ch.isClosing() )
                    {
                        finnalyFinishLogin( userCon, ignoreLoginSuccess );
                    }
                } );
            }
        };
        // fire login event
        bungee.getPluginManager().callEvent( new LoginEvent( InitialHandler.this, complete ) );
    }

    private void finnalyFinishLogin(UserConnection userCon, boolean ignoreLoginSuccess)
    {
        userCon.init();
        sendLoginSuccess( !ignoreLoginSuccess );
        ch.setProtocol( Protocol.GAME );

        ch.getHandle().pipeline().get( HandlerBoss.class ).setHandler( new UpstreamBridge( bungee, userCon ) ); //BotFilter

        bungee.getPluginManager().callEvent( new PostLoginEvent( userCon ) ); //BotFilter

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
        userCon.connect( server, null, true, ServerConnectEvent.Reason.JOIN_PROXY );
        thisState = State.FINISHED;
    }

    private void sendLoginSuccess(boolean send)
    {
        if ( send )
        {
            unsafe.sendPacket( new LoginSuccess( getUniqueId(), getName() ) );
        }
    }

    private boolean isInEventLoop()
    {
        return ch.getHandle().eventLoop().inEventLoop();
    }
    //BotFilter end

    @Override
    public void handle(LoginPayloadResponse response) throws Exception
    {
        disconnect( "Unexpected custom LoginPayloadResponse" );
    }

    @Override
    public void disconnect(String reason)
    {
        if ( canSendKickMessage() )
        {
            disconnect( TextComponent.fromLegacyText( reason ) );
        } else
        {
            ch.close();
        }
    }

    @Override
    public void disconnect(final BaseComponent... reason)
    {
        if ( canSendKickMessage() )
        {
            ch.delayedClose( new Kick( ComponentSerializer.toString( reason ) ) );
        } else
        {
            ch.close();
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
        return ( name != null ) ? name : ( loginRequest == null ) ? null : loginRequest.getData();
    }

    @Override
    public int getVersion()
    {
        return ( handshake == null ) ? -1 : handshake.getProtocolVersion();
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) getSocketAddress();
    }

    @Override
    public SocketAddress getSocketAddress()
    {
        return ch.getRemoteAddress();
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
    public void setUniqueId(UUID uuid)
    {
        Preconditions.checkState( thisState == State.USERNAME, "Can only set uuid while state is username" );
        Preconditions.checkState( !onlineMode, "Can only set uuid when online mode is false" );
        this.uniqueId = uuid;
    }

    @Override
    public String getUUID()
    {
        return uniqueId.toString().replace( "-", "" );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( '[' );

        String currentName = getName();
        if ( currentName != null )
        {
            sb.append( currentName );
            sb.append( ',' );
        }

        sb.append( getSocketAddress() );
        sb.append( "] <-> InitialHandler" );

        return sb.toString();
    }

    @Override
    public boolean isConnected()
    {
        return !ch.isClosed();
    }

    //BotFilter start
    public static void checkState(boolean expression, String errorMessage)
    {
        if ( !expression )
        {
            throw new FastException( errorMessage );
        }
    }
    //BotFilter end
}
