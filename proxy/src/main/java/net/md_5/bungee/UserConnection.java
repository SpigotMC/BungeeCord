package net.md_5.bungee;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.PlatformDependent;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Level;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectFailEvent;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.tab.TabListHandler;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.util.CaseInsensitiveSet;

@RequiredArgsConstructor
public final class UserConnection implements ProxiedPlayer
{

    /*========================================================================*/
    @NonNull
    private final ProxyServer bungee;
    @NonNull
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
    private boolean dimensionChange = true;
    @Getter
    private final Collection<ServerInfo> pendingConnects = new HashSet<>();
    /*========================================================================*/
    @Getter
    private TabListHandler tabList;
    @Getter
    @Setter
    private int sentPingId;
    @Getter
    @Setter
    private long sentPingTime;
    @Getter
    @Setter
    private int ping = 100;
    @Getter
    @Setter
    private ServerInfo reconnectServer;
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
    @Setter
    private ClientSettings settings;
    @Getter
    private final Scoreboard serverSentScoreboard = new Scoreboard();
    /*========================================================================*/
    @Getter
    private String displayName;
    @Getter
    private EntityMap entityRewrite;
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
        this.entityRewrite = new EntityMap( getPendingConnection().getVersion() );

        this.displayName = name;
        try
        {
            this.tabList = getPendingConnection().getListener().getTabList().getDeclaredConstructor().newInstance();
        } catch ( ReflectiveOperationException ex )
        {
            throw new RuntimeException( ex );
        }
        this.tabList.init( this );

        Collection<String> g = bungee.getConfigurationAdapter().getGroups( name );
        for ( String s : g )
        {
            addGroups( s );
        }
    }

    @Override
    public void setTabList(TabListHandler tabList)
    {
        tabList.init( this );
        this.tabList = tabList;
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
        Preconditions.checkArgument( name.length() <= 16, "Display name cannot be longer than 16 characters" );
        getTabList().onDisconnect();
        displayName = name;
        getTabList().onConnect();
    }

    @Override
    public void connect(ServerInfo target)
    {
        connect( target, null );
    }

    @Override
    public void connect(ServerInfo target, Callback<Boolean> callback)
    {
        connect( target, callback, false );
    }

    void sendDimensionSwitch()
    {
        dimensionChange = true;
        unsafe().sendPacket( PacketConstants.DIM1_SWITCH );
        unsafe().sendPacket( PacketConstants.DIM2_SWITCH );
    }

    public void connectNow(ServerInfo target)
    {
        sendDimensionSwitch();
        connect( target );
    }

    public void connect(ServerInfo info, final Callback<Boolean> callback, final boolean retry)
    {
        Preconditions.checkNotNull( info, "info" );

        ServerConnectEvent event = new ServerConnectEvent( this, info );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            return;
        }

        final BungeeServerInfo target = (BungeeServerInfo) event.getTarget(); // Update in case the event changed target

        if ( getServer() != null && Objects.equals( getServer().getInfo(), target ) )
        {
            sendMessage( bungee.getTranslation( "already_connected" ) );
            return;
        }
        if ( pendingConnects.contains( target ) )
        {
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
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( callback != null )
                {
                    callback.done( future.isSuccess(), future.cause() );
                }

                if ( !future.isSuccess() )
                {
                    future.channel().close();
                    pendingConnects.remove( target );
                    ServerInfo def = ProxyServer.getInstance().getServers().get( getPendingConnection().getListener().getFallbackServer() );
	                ServerConnectFailEvent failEvent = new ServerConnectFailEvent( UserConnection.this, target, def, future.cause(), true );
	                ProxyServer.getInstance().getPluginManager().callEvent( failEvent );

                    if ( retry && target != failEvent.getFallbackServer() && ( getServer() == null || failEvent.getFallbackServer() != getServer().getInfo() ) )
                    {
	                    if( failEvent.isSendMessage() )
	                    {
	                        sendMessage( bungee.getTranslation( "fallback_lobby" ) );
	                    }
                        connect( failEvent.getFallbackServer(), null, false );
                    } else
                    {
                        if ( dimensionChange )
                        {
                            disconnect( bungee.getTranslation( "fallback_kick" ) + future.cause().getClass().getName() );
                        } else
                        {
                            sendMessage( bungee.getTranslation( "fallback_kick" ) + future.cause().getClass().getName() );
                        }
                    }
                }
            }
        };
        Bootstrap b = new Bootstrap()
                .channel( NioSocketChannel.class )
                .group( ch.getHandle().eventLoop() )
                .handler( initializer )
                .option( ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000 ) // TODO: Configurable
                .remoteAddress( target.getAddress() );
        // Windows is bugged, multi homed users will just have to live with random connecting IPs
        if ( getPendingConnection().getListener().isSetLocalAddress() && !PlatformDependent.isWindows() )
        {
            b.localAddress( getPendingConnection().getListener().getHost().getHostString(), 0 );
        }
        b.connect().addListener( listener );
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

    public synchronized void disconnect0(BaseComponent... reason)
    {
        if ( ch.getHandle().isActive() )
        {
            bungee.getLogger().log( Level.INFO, "[" + getName() + "] disconnected with: " + BaseComponent.toLegacyText( reason ) );
            unsafe().sendPacket( new Kick( ComponentSerializer.toString( reason ) ) );
            ch.close();
            if ( server != null )
            {
                server.disconnect( "Quitting" );
            }
        }
    }

    @Override
    public void chat(String message)
    {
        Preconditions.checkState( server != null, "Not connected to server" );
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
        unsafe().sendPacket( new Chat( ComponentSerializer.toString( message ) ) );
    }

    @Override
    public void sendMessage(BaseComponent message)
    {
        unsafe().sendPacket( new Chat( ComponentSerializer.toString( message ) ) );
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        unsafe().sendPacket( new PluginMessage( channel, data ) );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) ch.getHandle().remoteAddress();
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
        return Collections.unmodifiableCollection(permissions);
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
}
