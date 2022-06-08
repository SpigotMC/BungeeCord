package net.md_5.bungee;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.PlatformDependent;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.SkinConfiguration;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.entitymap.EntityMap;
import net.md_5.bungee.forge.ForgeClientHandler;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.forge.ForgeServerHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.SetCompression;
import net.md_5.bungee.protocol.packet.SystemChat;
import net.md_5.bungee.tab.ServerUnique;
import net.md_5.bungee.tab.TabList;
import net.md_5.bungee.util.CaseInsensitiveSet;
import net.md_5.bungee.util.ChatComponentTransformer;

@RequiredArgsConstructor
public final class UserConnection implements ProxiedPlayer
{

    @Getter
    @Setter
    private boolean needLogin = true; //BotFilter
    @Getter
    @Setter
    private boolean callSettingsEvent = false; //BotFilter
    @Getter
    @Setter
    private List<PluginMessage> delayedPluginMessages = new ArrayList<>( ); //BotFilter

    /*========================================================================*/
    @NonNull
    private final ProxyServer bungee;
    @NonNull
    @Getter //BotFilter
    private final ChannelWrapper ch;
    @Getter
    @NonNull
    private final String name;
    @Getter
    private final InitialHandler pendingConnection;
    /*========================================================================*/
    @Getter
    @Setter
    private ServerConnection server;
    @Getter
    @Setter
    private Object dimension;
    @Getter
    @Setter
    private boolean dimensionChange = true;
    @Getter
    private final Collection<ServerInfo> pendingConnects = new HashSet<>();
    /*========================================================================*/
    @Getter
    @Setter
    private int ping = 100;
    @Getter
    @Setter
    private ServerInfo reconnectServer;
    @Getter
    private TabList tabListHandler;
    @Getter
    @Setter
    private int gamemode;
    @Getter
    private int compressionThreshold = -1;
    // Used for trying multiple servers in order
    @Setter
    private Queue<String> serverJoinQueue;
    /*========================================================================*/
    private final Collection<String> groups = new CaseInsensitiveSet();
    private final Collection<String> permissions = new CaseInsensitiveSet();
    /*========================================================================*/
    @Getter
    @Setter
    private int clientEntityId;
    @Getter
    @Setter
    private int serverEntityId;
    @Getter
    private ClientSettings settings;
    @Getter
    private final Scoreboard serverSentScoreboard = new Scoreboard();
    @Getter
    private final Collection<UUID> sentBossBars = new HashSet<>();
    /*========================================================================*/
    @Getter
    private String displayName;
    @Getter
    private EntityMap entityRewrite;
    private Locale locale;
    /*========================================================================*/
    @Getter
    @Setter
    private ForgeClientHandler forgeClientHandler;
    @Getter
    @Setter
    private ForgeServerHandler forgeServerHandler;
    /*========================================================================*/
    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }
    };

    public void init()
    {
        this.entityRewrite = EntityMap.getEntityMap( getPendingConnection().getVersion() );

        this.displayName = name;

        tabListHandler = new ServerUnique( this );

        Collection<String> g = bungee.getConfigurationAdapter().getGroups( name );
        g.addAll( bungee.getConfigurationAdapter().getGroups( getUniqueId().toString() ) );
        for ( String s : g )
        {
            addGroups( s );
        }

        forgeClientHandler = new ForgeClientHandler( this );

        // Set whether the connection has a 1.8 FML marker in the handshake.
        forgeClientHandler.setFmlTokenInHandshake( this.getPendingConnection().getExtraDataInHandshake().contains( ForgeConstants.FML_HANDSHAKE_TOKEN ) );
    }

    public void sendPacket(PacketWrapper packet)
    {
        ch.write( packet );
    }

    @Deprecated
    public boolean isActive()
    {
        return !ch.isClosed();
    }

    @Override
    public void setDisplayName(String name)
    {
        Preconditions.checkNotNull( name, "displayName" );
        displayName = name;
    }

    @Override
    public void connect(ServerInfo target)
    {
        connect( target, null, ServerConnectEvent.Reason.PLUGIN );
    }

    @Override
    public void connect(ServerInfo target, ServerConnectEvent.Reason reason)
    {
        connect( target, null, false, reason );
    }

    @Override
    public void connect(ServerInfo target, Callback<Boolean> callback)
    {
        connect( target, callback, false, ServerConnectEvent.Reason.PLUGIN );
    }

    @Override
    public void connect(ServerInfo target, Callback<Boolean> callback, ServerConnectEvent.Reason reason)
    {
        connect( target, callback, false, reason );
    }

    @Deprecated
    public void connectNow(ServerInfo target)
    {
        connectNow( target, ServerConnectEvent.Reason.UNKNOWN );
    }

    public void connectNow(ServerInfo target, ServerConnectEvent.Reason reason)
    {
        dimensionChange = true;
        connect( target, reason );
    }

    public ServerInfo updateAndGetNextServer(ServerInfo currentTarget)
    {
        if ( serverJoinQueue == null )
        {
            serverJoinQueue = new LinkedList<>( getPendingConnection().getListener().getServerPriority() );
        }

        ServerInfo next = null;
        while ( !serverJoinQueue.isEmpty() )
        {
            ServerInfo candidate = ProxyServer.getInstance().getServerInfo( serverJoinQueue.remove() );
            if ( !Objects.equals( currentTarget, candidate ) )
            {
                next = candidate;
                break;
            }
        }

        return next;
    }

    public void connect(ServerInfo info, final Callback<Boolean> callback, final boolean retry)
    {
        connect( info, callback, retry, ServerConnectEvent.Reason.PLUGIN );
    }

    public void connect(ServerInfo info, final Callback<Boolean> callback, final boolean retry, ServerConnectEvent.Reason reason)
    {
        Preconditions.checkNotNull( info, "info" );

        ServerConnectRequest.Builder builder = ServerConnectRequest.builder().retry( retry ).reason( reason ).target( info );
        if ( callback != null )
        {
            // Convert the Callback<Boolean> to be compatible with Callback<Result> from ServerConnectRequest.
            builder.callback( new Callback<ServerConnectRequest.Result>()
            {
                @Override
                public void done(ServerConnectRequest.Result result, Throwable error)
                {
                    callback.done( ( result == ServerConnectRequest.Result.SUCCESS ) ? Boolean.TRUE : Boolean.FALSE, error );
                }
            } );
        }

        connect( builder.build() );
    }

    @Override
    public void connect(final ServerConnectRequest request)
    {
        Preconditions.checkNotNull( request, "request" );

        final Callback<ServerConnectRequest.Result> callback = request.getCallback();
        ServerConnectEvent event = new ServerConnectEvent( this, request.getTarget(), request.getReason(), request );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            if ( callback != null )
            {
                callback.done( ServerConnectRequest.Result.EVENT_CANCEL, null );
            }

            if ( getServer() == null && !ch.isClosing() )
            {
                throw new IllegalStateException( "Cancelled ServerConnectEvent with no server or disconnect." );
            }
            return;
        }

        final BungeeServerInfo target = (BungeeServerInfo) event.getTarget(); // Update in case the event changed target

        if ( getServer() != null && Objects.equals( getServer().getInfo(), target ) )
        {
            if ( callback != null )
            {
                callback.done( ServerConnectRequest.Result.ALREADY_CONNECTED, null );
            }

            sendMessage( bungee.getTranslation( "already_connected" ) );
            return;
        }
        if ( pendingConnects.contains( target ) )
        {
            if ( callback != null )
            {
                callback.done( ServerConnectRequest.Result.ALREADY_CONNECTING, null );
            }

            sendMessage( bungee.getTranslation( "already_connecting" ) );
            return;
        }

        pendingConnects.add( target );

        ChannelInitializer initializer = new ChannelInitializer()
        {
            @Override
            protected void initChannel(Channel ch) throws Exception
            {
                PipelineUtils.BASE.initChannel( ch );
                ch.pipeline().addAfter( PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder( Protocol.HANDSHAKE, false, getPendingConnection().getVersion() ) );
                ch.pipeline().addAfter( PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder( Protocol.HANDSHAKE, false, getPendingConnection().getVersion() ) );
                ch.pipeline().get( HandlerBoss.class ).setHandler( new ServerConnector( bungee, UserConnection.this, target ) );
            }
        };
        ChannelFutureListener listener = new ChannelFutureListener()
        {
            @Override
            @SuppressWarnings("ThrowableResultIgnored")
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( callback != null )
                {
                    callback.done( ( future.isSuccess() ) ? ServerConnectRequest.Result.SUCCESS : ServerConnectRequest.Result.FAIL, future.cause() );
                }

                if ( !future.isSuccess() )
                {
                    future.channel().close();
                    pendingConnects.remove( target );

                    ServerInfo def = updateAndGetNextServer( target );
                    if ( request.isRetry() && def != null && ( getServer() == null || def != getServer().getInfo() ) )
                    {
                        sendMessage( bungee.getTranslation( "fallback_lobby" ) );
                        connect( def, null, true, ServerConnectEvent.Reason.LOBBY_FALLBACK );
                    } else if ( dimensionChange )
                    {
                        disconnect( bungee.getTranslation( "fallback_kick", connectionFailMessage( future.cause() ) ) );
                    } else
                    {
                        sendMessage( bungee.getTranslation( "fallback_kick", connectionFailMessage( future.cause() ) ) );
                    }
                }
            }
        };
        Bootstrap b = new Bootstrap()
                .channel( PipelineUtils.getChannel( target.getAddress() ) )
                .group( ch.getHandle().eventLoop() )
                .handler( initializer )
                .option( ChannelOption.CONNECT_TIMEOUT_MILLIS, request.getConnectTimeout() )
                .remoteAddress( target.getAddress() );
        // Windows is bugged, multi homed users will just have to live with random connecting IPs
        if ( getPendingConnection().getListener().isSetLocalAddress() && !PlatformDependent.isWindows() && getPendingConnection().getListener().getSocketAddress() instanceof InetSocketAddress )
        {
            b.localAddress( getPendingConnection().getListener().getHost().getHostString(), 0 );
        }
        b.connect().addListener( listener );
    }

    private String connectionFailMessage(Throwable cause)
    {
        return groups.contains( "admin" ) ? Util.exception( cause, false ) : cause.getClass().getName();
    }

    @Override
    public void disconnect(String reason)
    {
        disconnect0( TextComponent.fromLegacyText( reason ) );
    }

    @Override
    public void disconnect(BaseComponent... reason)
    {
        disconnect0( reason );
    }

    @Override
    public void disconnect(BaseComponent reason)
    {
        disconnect0( reason );
    }

    public void disconnect0(final BaseComponent... reason)
    {
        if ( !ch.isClosing() )
        {
            bungee.getLogger().log( Level.INFO, "[{0}] disconnected with: {1}", new Object[]
            {
                getName(), BaseComponent.toLegacyText( reason )
            } );

            ch.close( new Kick( ComponentSerializer.toString( reason ) ) );

            if ( server != null )
            {
                server.setObsolete( true );
                server.disconnect( "Quitting" );
            }
        }
    }

    @Override
    public void chat(String message)
    {
        Preconditions.checkState( server != null, "Not connected to server" );
        if ( getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_19 )
        {
            throw new UnsupportedOperationException( "Cannot spoof chat on this client version!" );
        }
        server.getCh().write( new Chat( message ) );
    }

    @Override
    public void sendMessage(String message)
    {
        sendMessage( TextComponent.fromLegacyText( message ) );
    }

    @Override
    public void sendMessages(String... messages)
    {
        for ( String message : messages )
        {
            sendMessage( message );
        }
    }

    @Override
    public void sendMessage(BaseComponent... message)
    {
        sendMessage( ChatMessageType.SYSTEM, message );
    }

    @Override
    public void sendMessage(BaseComponent message)
    {
        sendMessage( ChatMessageType.SYSTEM, message );
    }

    @Override
    public void sendMessage(ChatMessageType position, BaseComponent... message)
    {
        sendMessage( position, null, message );
    }

    @Override
    public void sendMessage(ChatMessageType position, BaseComponent message)
    {
        sendMessage( position, (UUID) null, message );
    }

    @Override
    public void sendMessage(UUID sender, BaseComponent... message)
    {
        sendMessage( ChatMessageType.CHAT, sender, message );
    }

    @Override
    public void sendMessage(UUID sender, BaseComponent message)
    {
        sendMessage( ChatMessageType.CHAT, sender, message );
    }

    private void sendMessage(ChatMessageType position, UUID sender, String message)
    {
        if ( getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_19 )
        {
            // Align with Spigot and remove client side formatting for now
            if ( position == ChatMessageType.CHAT )
            {
                position = ChatMessageType.SYSTEM;
            }

            unsafe().sendPacket( new SystemChat( message, position.ordinal() ) );
        } else
        {
            unsafe().sendPacket( new Chat( message, (byte) position.ordinal(), sender ) );
        }
    }

    private void sendMessage(ChatMessageType position, UUID sender, BaseComponent... message)
    {
        // transform score components
        message = ChatComponentTransformer.getInstance().transform( this, true, message );

        if ( position == ChatMessageType.ACTION_BAR && getPendingConnection().getVersion() < ProtocolConstants.MINECRAFT_1_17 )
        {
            // Versions older than 1.11 cannot send the Action bar with the new JSON formattings
            // Fix by converting to a legacy message, see https://bugs.mojang.com/browse/MC-119145
            if ( getPendingConnection().getVersion() <= ProtocolConstants.MINECRAFT_1_10 )
            {
                sendMessage( position, sender, ComponentSerializer.toString( new TextComponent( BaseComponent.toLegacyText( message ) ) ) );
            } else
            {
                net.md_5.bungee.protocol.packet.Title title = new net.md_5.bungee.protocol.packet.Title();
                title.setAction( net.md_5.bungee.protocol.packet.Title.Action.ACTIONBAR );
                title.setText( ComponentSerializer.toString( message ) );
                unsafe.sendPacket( title );
            }
        } else
        {
            sendMessage( position, sender, ComponentSerializer.toString( message ) );
        }
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        unsafe().sendPacket( new PluginMessage( channel, data, forgeClientHandler.isForgeUser() ) );
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
    public Collection<String> getGroups()
    {
        return Collections.unmodifiableCollection( groups );
    }

    @Override
    public void addGroups(String... groups)
    {
        for ( String group : groups )
        {
            this.groups.add( group );
            for ( String permission : bungee.getConfigurationAdapter().getPermissions( group ) )
            {
                setPermission( permission, true );
            }
        }
    }

    @Override
    public void removeGroups(String... groups)
    {
        for ( String group : groups )
        {
            this.groups.remove( group );
            for ( String permission : bungee.getConfigurationAdapter().getPermissions( group ) )
            {
                setPermission( permission, false );
            }
        }
    }

    @Override
    public boolean hasPermission(String permission)
    {
        return bungee.getPluginManager().callEvent( new PermissionCheckEvent( this, permission, permissions.contains( permission ) ) ).hasPermission();
    }

    @Override
    public void setPermission(String permission, boolean value)
    {
        if ( value )
        {
            permissions.add( permission );
        } else
        {
            permissions.remove( permission );
        }
    }

    @Override
    public Collection<String> getPermissions()
    {
        return Collections.unmodifiableCollection( permissions );
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }

    @Override
    public String getUUID()
    {
        return getPendingConnection().getUUID();
    }

    @Override
    public UUID getUniqueId()
    {
        return getPendingConnection().getUniqueId();
    }

    public void setSettings(ClientSettings settings)
    {
        this.settings = settings;
        this.locale = null;
    }

    @Override
    public Locale getLocale()
    {
        return ( locale == null && settings != null ) ? locale = Locale.forLanguageTag( settings.getLocale().replace( '_', '-' ) ) : locale;
    }

    @Override
    public byte getViewDistance()
    {
        return ( settings != null ) ? settings.getViewDistance() : 10;
    }

    @Override
    public ProxiedPlayer.ChatMode getChatMode()
    {
        if ( settings == null )
        {
            return ProxiedPlayer.ChatMode.SHOWN;
        }

        switch ( settings.getChatFlags() )
        {
            default:
            case 0:
                return ProxiedPlayer.ChatMode.SHOWN;
            case 1:
                return ProxiedPlayer.ChatMode.COMMANDS_ONLY;
            case 2:
                return ProxiedPlayer.ChatMode.HIDDEN;
        }
    }

    @Override
    public boolean hasChatColors()
    {
        return settings == null || settings.isChatColours();
    }

    @Override
    public SkinConfiguration getSkinParts()
    {
        return ( settings != null ) ? new PlayerSkinConfiguration( settings.getSkinParts() ) : PlayerSkinConfiguration.SKIN_SHOW_ALL;
    }

    @Override
    public ProxiedPlayer.MainHand getMainHand()
    {
        return ( settings == null || settings.getMainHand() == 1 ) ? ProxiedPlayer.MainHand.RIGHT : ProxiedPlayer.MainHand.LEFT;
    }

    @Override
    public boolean isForgeUser()
    {
        return forgeClientHandler.isForgeUser();
    }

    @Override
    public Map<String, String> getModList()
    {
        if ( forgeClientHandler.getClientModList() == null )
        {
            // Return an empty map, rather than a null, if the client hasn't got any mods,
            // or is yet to complete a handshake.
            return ImmutableMap.of();
        }

        return ImmutableMap.copyOf( forgeClientHandler.getClientModList() );
    }

    @Override
    public void setTabHeader(BaseComponent header, BaseComponent footer)
    {
        header = ChatComponentTransformer.getInstance().transform( this, true, header )[0];
        footer = ChatComponentTransformer.getInstance().transform( this, true, footer )[0];

        unsafe().sendPacket( new PlayerListHeaderFooter(
                ComponentSerializer.toString( header ),
                ComponentSerializer.toString( footer )
        ) );
    }

    @Override
    public void setTabHeader(BaseComponent[] header, BaseComponent[] footer)
    {
        header = ChatComponentTransformer.getInstance().transform( this, true, header );
        footer = ChatComponentTransformer.getInstance().transform( this, true, footer );

        unsafe().sendPacket( new PlayerListHeaderFooter(
                ComponentSerializer.toString( header ),
                ComponentSerializer.toString( footer )
        ) );
    }

    @Override
    public void resetTabHeader()
    {
        // Mojang did not add a way to remove the header / footer completely, we can only set it to empty
        setTabHeader( (BaseComponent) null, null );
    }

    @Override
    public void sendTitle(Title title)
    {
        title.send( this );
    }

    public String getExtraDataInHandshake()
    {
        return this.getPendingConnection().getExtraDataInHandshake();
    }

    public void setCompressionThreshold(int compressionThreshold)
    {
        if ( !ch.isClosing() && this.compressionThreshold == -1 && compressionThreshold >= 0 )
        {
            this.compressionThreshold = compressionThreshold;
            unsafe.sendPacket( new SetCompression( compressionThreshold ) );
            ch.setCompressionThreshold( compressionThreshold );
        }
    }

    @Override
    public boolean isConnected()
    {
        return !ch.isClosed();
    }

    @Override
    public Scoreboard getScoreboard()
    {
        return serverSentScoreboard;
    }

    //BotFilter start
    public void addDelayedPluginMessage(PluginMessage message)
    {
        if ( delayedPluginMessages.size() >= 150 )
        {
            throw new IndexOutOfBoundsException();
        }
        delayedPluginMessages.add( message );
    }
    //BotFilter end
}
