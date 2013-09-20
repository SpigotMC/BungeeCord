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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Objects;
import java.util.logging.Level;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.tab.TabListHandler;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketWrapper;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.packet.DefinedPacket;
import net.md_5.bungee.protocol.packet.Packet3Chat;
import net.md_5.bungee.protocol.packet.PacketCCSettings;
import net.md_5.bungee.protocol.packet.PacketFAPluginMessage;
import net.md_5.bungee.protocol.packet.PacketFFKick;
import net.md_5.bungee.util.CaseInsensitiveSet;

@RequiredArgsConstructor
public final class UserConnection implements ProxiedPlayer
{

    /*========================================================================*/
    @NonNull
    private final ProxyServer bungee;
    @NonNull
    @Getter
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
    private final Object switchMutex = new Object();
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
    private PacketCCSettings settings;
    @Getter
    private final Scoreboard serverSentScoreboard = new Scoreboard();
    /*========================================================================*/
    @Getter
    private String displayName;
    private InetSocketAddress effectiveAddress = null;
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
        connect( target, false );
    }

    void sendDimensionSwitch()
    {
        unsafe().sendPacket( PacketConstants.DIM1_SWITCH );
        unsafe().sendPacket( PacketConstants.DIM2_SWITCH );
    }

    public void connectNow(ServerInfo target)
    {
        sendDimensionSwitch();
        connect( target, true );
    }

    public void connect(ServerInfo info, final boolean retry)
    {
        connect(info, retry, 0);
    }
    
    public void connect(ServerInfo info, final boolean retry, final int retryCount)
    {
        ServerConnectEvent event = new ServerConnectEvent( this, info );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            return;
        }

        final BungeeServerInfo target = (BungeeServerInfo) event.getTarget(); // Update in case the event changed target

        if ( getServer() != null && Objects.equals( getServer().getInfo(), target ) && ! getServer().isObsolete() )
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
                ch.pipeline().get( HandlerBoss.class ).setHandler( new ServerConnector( bungee, UserConnection.this, target ) );
            }
        };
        ChannelFutureListener listener = new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( !future.isSuccess() )
                {
                    future.channel().close();
                    pendingConnects.remove( target );

                    final ServerInfo def = ProxyServer.getInstance().getServers().get( getPendingConnection().getListener().getFallbackServer() );
                    if ( retry && target != def && ( getServer() == null || def != getServer().getInfo() ) && isActive() )
                    {
                        sendMessage( bungee.getTranslation( "fallback_lobby" ) );
                        connect( def, false );
                    } else if ( target == def && retry && retryCount <= 12 && isActive() )
                    {
                        ch.getHandle().eventLoop().schedule( new Runnable() {
                            @Override
                            public void run() {
                                connect( def, true, retryCount + 1 );
                            }
                        }, 5, TimeUnit.SECONDS );
                    } else
                    {
                        if ( server == null )
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
                .group( BungeeCord.getInstance().eventLoops )
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
    public synchronized void disconnect(String reason)
    {
        if ( !ch.isClosed() )
        {
            bungee.getLogger().log( Level.INFO, "[" + getName() + "] disconnected with: " + reason );
            unsafe().sendPacket( new PacketFFKick( reason ) );
            if ( server != null )
            {
                server.setObsolete( true );
                server.disconnect( "Quitting" );
            }
            ch.close();
        }
    }

    @Override
    public void chat(String message)
    {
        Preconditions.checkState( server != null, "Not connected to server" );
        server.getCh().write( new Packet3Chat( message ) );
    }

    @Override
    public void sendMessage(String message)
    {
        // TODO: Fix this
        String encoded = BungeeCord.getInstance().gson.toJson( message );
        unsafe().sendPacket( new Packet3Chat( "{\"text\":" + encoded + "}" ) );
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
    public void sendData(String channel, byte[] data)
    {
        unsafe().sendPacket( new PacketFAPluginMessage( channel, data ) );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) ch.getHandle().remoteAddress();
    }
    
    public InetSocketAddress getEffectiveAddress() {
    	if (effectiveAddress == null)
    		return getAddress();
    	return effectiveAddress;
    }
    
    public void setEffectiveAddress(InetSocketAddress addr) {
    	effectiveAddress = addr;
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
    public String toString()
    {
        return name;
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }
}
