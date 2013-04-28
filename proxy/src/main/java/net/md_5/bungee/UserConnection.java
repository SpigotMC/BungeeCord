package net.md_5.bungee;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Level;
import lombok.AccessLevel;
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
import net.md_5.bungee.api.scoreboard.Scoreboard;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.Packet9Respawn;
import net.md_5.bungee.packet.PacketCCSettings;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFFKick;

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
    @NonNull
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
    @Setter
    private int sentPingId;
    @Getter
    @Setter
    private long sentPingTime;
    @Getter
    @Setter
    private int ping = 100;
    /*========================================================================*/
    private final Collection<String> groups = new HashSet<>();
    private final Collection<String> permissions = new HashSet<>();
    /*========================================================================*/
    @Getter
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
    /*========================================================================*/

    public void init()
    {
        this.displayName = name;

        Collection<String> g = bungee.getConfigurationAdapter().getGroups( name );
        for ( String s : g )
        {
            addGroups( s );
        }
    }

    public void sendPacket(DefinedPacket p)
    {
        ch.write( p );
    }

    public void sendPacket(byte[] b)
    {
        ch.write( b );
    }

    @Deprecated
    public boolean isActive()
    {
        return ch.getHandle().isActive();
    }

    @Override
    public void setDisplayName(String name)
    {
        Preconditions.checkNotNull( name, "displayName" );
        Preconditions.checkArgument( name.length() <= 16, "Display name cannot be longer than 16 characters" );
        bungee.getTabListHandler().onDisconnect( this );
        displayName = name;
        bungee.getTabListHandler().onConnect( this );
    }

    @Override
    public void connect(ServerInfo target)
    {
        connect( target, false );
    }

    public void connectNow(ServerInfo target)
    {
        sendPacket( Packet9Respawn.DIM1_SWITCH );
        sendPacket( Packet9Respawn.DIM2_SWITCH );
        connect( target );
    }

    public void connect(ServerInfo info, final boolean retry)
    {
        ServerConnectEvent event = new ServerConnectEvent( this, info );
        ProxyServer.getInstance().getPluginManager().callEvent( event );

        Preconditions.checkArgument( event.getTarget() instanceof BungeeServerInfo, "BungeeCord can only connect to BungeeServerInfo instances" );
        final BungeeServerInfo target = (BungeeServerInfo) event.getTarget(); // Update in case the event changed target

        if ( getServer() != null && Objects.equals( getServer().getInfo(), target ) )
        {
            sendMessage( ChatColor.RED + "Cannot connect to server you are already on!" );
            return;
        }
        if ( pendingConnects.contains( target ) )
        {
            sendMessage( ChatColor.RED + "Already connecting to this server!" );
            return;
        }

        pendingConnects.add( target );

        new Bootstrap()
                .channel( NioSocketChannel.class )
                .group( BungeeCord.getInstance().eventLoops )
                .handler( new ChannelInitializer()
        {
            @Override
            protected void initChannel(Channel ch) throws Exception
            {
                PipelineUtils.BASE.initChannel( ch );
                ch.pipeline().get( HandlerBoss.class ).setHandler( new ServerConnector( bungee, UserConnection.this, target ) );
            }
        } )
                .option( ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000 ) // TODO: Configurable
                .remoteAddress( target.getAddress() )
                .connect().addListener( new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( !future.isSuccess() )
                {
                    future.channel().close();
                    pendingConnects.remove( target );

                    ServerInfo def = ProxyServer.getInstance().getServers().get( getPendingConnection().getListener().getFallbackServer() );
                    if ( retry & target != def && ( getServer() == null || def != getServer().getInfo() ) )
                    {
                        sendMessage( ChatColor.RED + "Could not connect to target server, you have been moved to the lobby server" );
                        connect( def, false );
                    } else
                    {
                        if ( server == null )
                        {
                            disconnect( "Could not connect to default server, please try again later: " + future.cause().getClass().getName() );
                        } else
                        {
                            sendMessage( ChatColor.RED + "Could not connect to selected server, please try again later: " + future.cause().getClass().getName() );
                        }
                    }
                }
            }
        } );
    }

    @Override
    public synchronized void disconnect(String reason)
    {
        if ( ch.getHandle().isActive() )
        {
            bungee.getLogger().log( Level.INFO, "[" + getName() + "] disconnected with: " + reason );
            sendPacket( new PacketFFKick( reason ) );
            ch.getHandle().close();
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
        server.getCh().write( new Packet3Chat( message ) );
    }

    @Override
    public void sendMessage(String message)
    {
        sendPacket( new Packet3Chat( message ) );
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
        sendPacket( new PacketFAPluginMessage( channel, data ) );
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
    public String toString()
    {
        return name;
    }

    public void setClientEntityId(int clientEntityId)
    {
        Preconditions.checkState( this.clientEntityId == 0, "Client entityId already set!" );
        this.clientEntityId = clientEntityId;
    }
}
