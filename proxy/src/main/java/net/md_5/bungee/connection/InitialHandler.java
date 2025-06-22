package net.md_5.bungee.connection;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.crypto.SecretKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
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
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.CookieRequest;
import net.md_5.bungee.protocol.packet.CookieResponse;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LegacyHandshake;
import net.md_5.bungee.protocol.packet.LegacyPing;
import net.md_5.bungee.protocol.packet.LoginAcknowledged;
import net.md_5.bungee.protocol.packet.LoginPayloadRequest;
import net.md_5.bungee.protocol.packet.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.util.AllowedCharacters;
import net.md_5.bungee.util.BufUtil;
import net.md_5.bungee.util.QuietException;

@RequiredArgsConstructor
public class InitialHandler extends PacketHandler implements PendingConnection
{

    private final BungeeCord bungee;
    private ChannelWrapper ch;
    @Getter
    private final ListenerInfo listener;
    @Getter
    private Handshake handshake;
    @Getter
    private LoginRequest loginRequest;
    private EncryptionRequest request;
    @Getter
    private PluginMessage brandMessage;
    @Getter
    private String clientBrand;
    @Getter
    private final Set<String> registeredChannels = new HashSet<>();
    private State thisState = State.HANDSHAKE;
    private int loginPayloadId;
    private final Map<Integer, CompletableFuture<byte[]>> requestedLoginPayloads = new HashMap<>();
    private final Queue<CookieFuture> requestedCookies = new LinkedList<>();

    @Data
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class CookieFuture
    {

        private String cookie;
        private CompletableFuture<byte[]> future;
    }

    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }

        @Override
        public void sendPacketQueued(DefinedPacket packet)
        {
            throw new UnsupportedOperationException( "Not supported" );
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
    private UUID rewriteId;
    @Getter
    private LoginResult loginProfile;
    @Getter
    private boolean legacy;
    @Getter
    private String extraDataInHandshake = "";
    @Getter
    private boolean transferred;
    private UserConnection userCon;

    @Override
    public boolean shouldHandle(PacketWrapper packet) throws Exception
    {
        return !ch.isClosing();
    }

    private enum State
    {

        HANDSHAKE, STATUS, PING, USERNAME, ENCRYPT, FINISHING;
    }

    private boolean canSendKickMessage()
    {
        return thisState == State.USERNAME || thisState == State.ENCRYPT || thisState == State.FINISHING;
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
        if ( packet.packet == null )
        {
            throw new QuietException( "Unexpected packet received during login process! " + BufUtil.dump( packet.buf, 16 ) );
        }
    }

    @Override
    public void handle(LegacyHandshake legacyHandshake) throws Exception
    {
        Preconditions.checkState( !this.legacy, "Not expecting LegacyHandshake" );
        this.legacy = true;
        ch.close( bungee.getTranslation( "outdated_client", bungee.getGameVersion() ) );
    }

    @Override
    public void handle(LegacyPing ping) throws Exception
    {
        Preconditions.checkState( !this.legacy, "Not expecting LegacyPing" );
        this.legacy = true;
        final boolean v1_5 = ping.isV1_5();

        ServerInfo forced = AbstractReconnectHandler.getForcedHost( this );
        final String motd = ( forced != null ) ? forced.getMotd() : listener.getMotd();
        final int protocol = bungee.getProtocolVersion();

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
                    public void done(ProxyPingEvent result, Throwable error)
                    {
                        ServerPing legacy = result.getResponse();
                        String kickMessage;

                        if ( v1_5 )
                        {
                            kickMessage = ChatColor.DARK_BLUE
                                    + "\00" + 127
                                    + '\00' + legacy.getVersion().getName()
                                    + '\00' + getFirstLine( legacy.getDescription() )
                                    + '\00' + ( ( legacy.getPlayers() != null ) ? legacy.getPlayers().getOnline() : "-1" )
                                    + '\00' + ( ( legacy.getPlayers() != null ) ? legacy.getPlayers().getMax() : "-1" );
                        } else
                        {
                            // Clients <= 1.3 don't support colored motds because the color char is used as delimiter
                            kickMessage = ChatColor.stripColor( getFirstLine( legacy.getDescription() ) )
                                    + '\u00a7' + ( ( legacy.getPlayers() != null ) ? legacy.getPlayers().getOnline() : "-1" )
                                    + '\u00a7' + ( ( legacy.getPlayers() != null ) ? legacy.getPlayers().getMax() : "-1" );
                        }

                        ch.close( kickMessage );
                    }
                };

                bungee.getPluginManager().callEvent( new ProxyPingEvent( InitialHandler.this, result, eventLoopCallback( callback ) ) );
            }
        };

        if ( forced != null && listener.isPingPassthrough() )
        {
            ( (BungeeServerInfo) forced ).ping( pingBack, bungee.getProtocolVersion() );
        } else
        {
            pingBack.done( getPingInfo( motd, protocol ), null );
        }
    }

    private static String getFirstLine(String str)
    {
        int pos = str.indexOf( '\n' );
        return pos == -1 ? str : str.substring( 0, pos );
    }

    private ServerPing getPingInfo(String motd, int protocol)
    {
        return new ServerPing(
                new ServerPing.Protocol( bungee.getName() + " " + bungee.getGameVersion(), protocol ),
                new ServerPing.Players( listener.getMaxPlayers(), bungee.getOnlineCount(), null ),
                motd, BungeeCord.getInstance().config.getFaviconObject()
        );
    }

    @Override
    public void handle(StatusRequest statusRequest) throws Exception
    {
        Preconditions.checkState( thisState == State.STATUS, "Not expecting STATUS" );
        thisState = null; // don't accept multiple status requests and set state to ping in async event callback

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
                        Gson gson = PingHandler.gson;
                        unsafe.sendPacket( new StatusResponse( gson.toJson( pingResult.getResponse() ) ) );
                        thisState = State.PING;
                    }
                };

                bungee.getPluginManager().callEvent( new ProxyPingEvent( InitialHandler.this, result, eventLoopCallback( callback ) ) );
            }
        };

        if ( forced != null && listener.isPingPassthrough() )
        {
            ( (BungeeServerInfo) forced ).ping( pingBack, handshake.getProtocolVersion() );
        } else
        {
            pingBack.done( getPingInfo( motd, protocol ), null );
        }
    }

    @Override
    public void handle(PingPacket ping) throws Exception
    {
        Preconditions.checkState( thisState == State.PING, "Not expecting PING" );
        unsafe.sendPacket( ping );
        disconnect( "" );
        if ( bungee.getConnectionThrottle() != null )
        {
            bungee.getConnectionThrottle().unthrottle( getSocketAddress() );
        }
    }

    @Override
    public void handle(Handshake handshake) throws Exception
    {
        Preconditions.checkState( thisState == State.HANDSHAKE && !this.legacy, "Not expecting HANDSHAKE" );
        this.handshake = handshake;
        ch.setVersion( handshake.getProtocolVersion() );
        ch.getHandle().pipeline().remove( PipelineUtils.LEGACY_KICKER );

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

        // return if the connection was closed during the event
        if ( ch.isClosing() )
        {
            return;
        }

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
                break;
            case 2:
            case 3:
                transferred = handshake.getRequestedProtocol() == 3;
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

                if ( transferred && bungee.config.isRejectTransfers() )
                {
                    disconnect( bungee.getTranslation( "reject_transfer" ) );
                    return;
                }
                break;
            default:
                throw new QuietException( "Cannot request protocol " + handshake.getRequestedProtocol() );
        }
    }

    @Override
    public void handle(LoginRequest loginRequest) throws Exception
    {
        Preconditions.checkState( thisState == State.USERNAME && this.loginRequest == null, "Not expecting USERNAME" );

        if ( !AllowedCharacters.isValidName( loginRequest.getData(), onlineMode ) )
        {
            disconnect( bungee.getTranslation( "name_invalid" ) );
            return;
        }

        if ( BungeeCord.getInstance().config.isEnforceSecureProfile() && getVersion() < ProtocolConstants.MINECRAFT_1_19_3 )
        {
            PlayerPublicKey publicKey = loginRequest.getPublicKey();
            if ( publicKey == null )
            {
                disconnect( bungee.getTranslation( "secure_profile_required" ) );
                return;
            }

            if ( Instant.ofEpochMilli( publicKey.getExpiry() ).isBefore( Instant.now() ) )
            {
                disconnect( bungee.getTranslation( "secure_profile_expired" ) );
                return;
            }

            if ( getVersion() < ProtocolConstants.MINECRAFT_1_19_1 )
            {
                if ( !EncryptionUtil.check( publicKey, null ) )
                {
                    disconnect( bungee.getTranslation( "secure_profile_invalid" ) );
                    return;
                }
            }
        }

        this.loginRequest = loginRequest;

        int limit = BungeeCord.getInstance().config.getPlayerLimit();
        if ( limit > 0 && bungee.getOnlineCount() >= limit )
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
                    BaseComponent reason = result.getReason();
                    disconnect( ( reason != null ) ? reason : TextComponent.fromLegacy( bungee.getTranslation( "kick_message" ) ) );
                    return;
                }
                if ( onlineMode )
                {
                    thisState = State.ENCRYPT;
                    unsafe().sendPacket( request = EncryptionUtil.encryptRequest() );
                } else
                {
                    thisState = State.FINISHING;
                    finish();
                }
            }
        };

        // fire pre login event
        bungee.getPluginManager().callEvent( new PreLoginEvent( InitialHandler.this, eventLoopCallback( callback ) ) );
    }

    @Override
    public void handle(final EncryptionResponse encryptResponse) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT, "Not expecting ENCRYPT" );
        Preconditions.checkState( EncryptionUtil.check( loginRequest.getPublicKey(), encryptResponse, request ), "Invalid verification" );

        SecretKey sharedKey = EncryptionUtil.getSecret( encryptResponse, request );
        BungeeCipher decrypt = EncryptionUtil.getCipher( false, sharedKey );
        ch.addBefore( PipelineUtils.FRAME_DECODER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder( decrypt ) );
        BungeeCipher encrypt = EncryptionUtil.getCipher( true, sharedKey );
        ch.addBefore( PipelineUtils.FRAME_PREPENDER_AND_COMPRESS, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder( encrypt ) );
        // disable use of composite buffers if we use natives
        ch.updateComposite();

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

        String preventProxy = ( BungeeCord.getInstance().config.isPreventProxyConnections() && getSocketAddress() instanceof InetSocketAddress ) ? "&ip=" + URLEncoder.encode( getAddress().getAddress().getHostAddress(), "UTF-8" ) : "";
        String authURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encName + "&serverId=" + encodedHash + preventProxy;

        Callback<String> handler = new Callback<String>()
        {
            @Override
            public void done(String result, Throwable error)
            {
                if ( error == null )
                {
                    LoginResult obj = LoginResult.GSON.fromJson( result, LoginResult.class );
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
        thisState = State.FINISHING;
        HttpClient.get( authURL, ch.getHandle().eventLoop(), handler );
    }

    private void finish()
    {
        offlineId = UUID.nameUUIDFromBytes( ( "OfflinePlayer:" + getName() ).getBytes( StandardCharsets.UTF_8 ) );
        if ( uniqueId == null )
        {
            uniqueId = offlineId;
        }
        rewriteId = ( bungee.config.isIpForward() ) ? uniqueId : offlineId;

        if ( BungeeCord.getInstance().config.isEnforceSecureProfile() )
        {
            if ( getVersion() >= ProtocolConstants.MINECRAFT_1_19_1 && getVersion() < ProtocolConstants.MINECRAFT_1_19_3 )
            {
                boolean secure = false;
                try
                {
                    secure = EncryptionUtil.check( loginRequest.getPublicKey(), uniqueId );
                } catch ( GeneralSecurityException ex )
                {
                }

                if ( !secure )
                {
                    disconnect( bungee.getTranslation( "secure_profile_invalid" ) );
                    return;
                }
            }
        }

        ProxiedPlayer oldName = bungee.getPlayer( getName() );
        if ( oldName != null )
        {
            // TODO See #1218
            disconnect( bungee.getTranslation( "already_connected_proxy" ) );
            return;
        }

        if ( isOnlineMode() )
        {
            // And then also for their old UUID
            ProxiedPlayer oldID = bungee.getPlayer( getUniqueId() );
            if ( oldID != null )
            {
                // TODO See #1218
                disconnect( bungee.getTranslation( "already_connected_proxy" ) );
                return;
            }
        }

        Callback<LoginEvent> complete = new Callback<LoginEvent>()
        {
            @Override
            public void done(LoginEvent result, Throwable error)
            {
                if ( result.isCancelled() )
                {
                    BaseComponent reason = result.getReason();
                    disconnect( ( reason != null ) ? reason : TextComponent.fromLegacy( bungee.getTranslation( "kick_message" ) ) );
                    return;
                }

                userCon = new UserConnection( bungee, ch, getName(), InitialHandler.this );
                userCon.setCompressionThreshold( BungeeCord.getInstance().config.getCompressionThreshold() );

                if ( getVersion() < ProtocolConstants.MINECRAFT_1_20_2 )
                {
                    unsafe.sendPacket( new LoginSuccess( getRewriteId(), getName(), ( loginProfile == null ) ? null : loginProfile.getProperties() ) );
                    ch.setProtocol( Protocol.GAME );
                }
                finish2();
            }
        };

        // fire login event
        bungee.getPluginManager().callEvent( new LoginEvent( InitialHandler.this, eventLoopCallback( complete ) ) );
    }

    private void finish2()
    {
        if ( !userCon.init() )
        {
            disconnect( bungee.getTranslation( "already_connected_proxy" ) );
            return;
        }

        ch.getHandle().pipeline().get( HandlerBoss.class ).setHandler( new UpstreamBridge( bungee, userCon ) );

        ServerInfo initialServer;
        if ( bungee.getReconnectHandler() != null )
        {
            initialServer = bungee.getReconnectHandler().getServer( userCon );
        } else
        {
            initialServer = AbstractReconnectHandler.getForcedHost( InitialHandler.this );
        }
        if ( initialServer == null )
        {
            initialServer = bungee.getServerInfo( listener.getDefaultServer() );
        }

        Callback<PostLoginEvent> complete = new Callback<PostLoginEvent>()
        {
            @Override
            public void done(PostLoginEvent result, Throwable error)
            {
                userCon.connect( result.getTarget(), null, true, ServerConnectEvent.Reason.JOIN_PROXY );
            }
        };

        // fire post-login event
        bungee.getPluginManager().callEvent( new PostLoginEvent( userCon, initialServer, eventLoopCallback( complete ) ) );
    }

    @Override
    public void handle(LoginPayloadResponse response) throws Exception
    {
        CompletableFuture<byte[]> future;
        synchronized ( requestedLoginPayloads )
        {
            future = requestedLoginPayloads.remove( response.getId() );
        }
        Preconditions.checkState( future != null, "Unexpected custom LoginPayloadResponse" );
        future.complete( response.getData() );

        // we should never pass this to the backend
        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public void handle(LoginAcknowledged loginAcknowledged) throws Exception
    {
        // this packet should only be sent after the login success (it should be handled in the UpstreamBridge)
        disconnect( "Unexpected LoginAcknowledged" );
    }

    @Override
    public void handle(CookieResponse cookieResponse)
    {
        // be careful, backend server could also make the client send a cookie response
        CookieFuture future;
        synchronized ( requestedCookies )
        {
            future = requestedCookies.peek();
            if ( future != null )
            {
                if ( future.cookie.equals( cookieResponse.getCookie() ) )
                {
                    Preconditions.checkState( future == requestedCookies.poll(), "requestedCookies queue mismatch" );
                } else
                {
                    future = null; // leave for handling by backend
                }
            }
        }

        if ( future != null )
        {
            future.getFuture().complete( cookieResponse.getData() );

            throw CancelSendSignal.INSTANCE;
        }

        // if there is no userCon we can't have a connection to a backend server that could have requested this cookie
        // which means that this cookie is invalid as the proxy also has not requested it
        Preconditions.checkState( userCon != null, "not requested cookie received" );
    }

    @Override
    public void disconnect(String reason)
    {
        if ( canSendKickMessage() )
        {
            disconnect( TextComponent.fromLegacy( reason ) );
        } else
        {
            ch.close();
        }
    }

    @Override
    public void disconnect(final BaseComponent... reason)
    {
        disconnect( TextComponent.fromArray( reason ) );
    }

    @Override
    public void disconnect(BaseComponent reason)
    {
        if ( canSendKickMessage() )
        {
            ch.delayedClose( new Kick( reason ) );
        } else
        {
            ch.close();
        }
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

    public void relayMessage(PluginMessage input) throws Exception
    {
        if ( input.getTag().equals( "REGISTER" ) || input.getTag().equals( "minecraft:register" ) )
        {
            String content = new String( input.getData(), StandardCharsets.UTF_8 );

            for ( String id : content.split( "\0" ) )
            {
                Preconditions.checkState( registeredChannels.size() < 128, "Too many registered channels" );
                Preconditions.checkArgument( id.length() < 128, "Channel name too long" );

                registeredChannels.add( id );
            }
        } else if ( input.getTag().equals( "UNREGISTER" ) || input.getTag().equals( "minecraft:unregister" ) )
        {
            String content = new String( input.getData(), StandardCharsets.UTF_8 );

            for ( String id : content.split( "\0" ) )
            {
                registeredChannels.remove( id );
            }
        } else if ( input.getTag().equals( "MC|Brand" ) || input.getTag().equals( "minecraft:brand" ) )
        {
            brandMessage = input;
            clientBrand = DefinedPacket.readString( Unpooled.wrappedBuffer( input.getData() ) );
        }
    }

    @Override
    public CompletableFuture<byte[]> retrieveCookie(String cookie)
    {
        Preconditions.checkState( getVersion() >= ProtocolConstants.MINECRAFT_1_20_5, "Cookies are only supported in 1.20.5 and above" );
        Preconditions.checkState( loginRequest != null, "Cannot retrieve cookies for status or legacy connections" );

        if ( cookie.indexOf( ':' ) == -1 )
        {
            // if we request an invalid resource location (no prefix) the client will respond with "minecraft:" prefix
            cookie = "minecraft:" + cookie;
        }

        CompletableFuture<byte[]> future = new CompletableFuture<>();
        synchronized ( requestedCookies )
        {
            requestedCookies.add( new CookieFuture( cookie, future ) );
        }
        unsafe.sendPacket( new CookieRequest( cookie ) );

        return future;
    }

    @Override
    public CompletableFuture<byte[]> sendData(String channel, byte[] data)
    {
        Preconditions.checkState( getVersion() >= ProtocolConstants.MINECRAFT_1_13, "LoginPayloads are only supported in 1.13 and above" );
        Preconditions.checkState( ch.getEncodeProtocol() == Protocol.LOGIN, "LoginPayloads are only supported in the login phase" );

        CompletableFuture<byte[]> future = new CompletableFuture<>();
        final int id;
        synchronized ( requestedLoginPayloads )
        {
            // thread safe loginPayloadId
            id = loginPayloadId++;
            requestedLoginPayloads.put( id, future );
        }
        unsafe.sendPacket( new LoginPayloadRequest( id, channel, data ) );
        return future;
    }

    // this method is used for event execution
    // if this connection is disconnected during an event-call, the original callback is not called
    // if the event was executed async, we execute the callback on the eventloop again
    // otherwise netty will schedule any pipeline related call by itself, this decreases performance
    private <T> Callback<T> eventLoopCallback(Callback<T> callback)
    {
        return (result, error) ->
        {
            ch.scheduleIfNecessary( () ->
            {
                if ( !ch.isClosing() )
                {
                    callback.done( result, error );
                }
            } );
        };
    }
}
