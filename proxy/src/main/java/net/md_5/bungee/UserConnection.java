package net.md_5.bungee;

import com.google.common.base.Preconditions;
import gnu.trove.set.hash.THashSet;
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
import java.util.List;
import java.util.logging.Level;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.packet.*;

public final class UserConnection implements ProxiedPlayer
{

    public final Packet2Handshake handshake;
    private final ProxyServer bungee;
    public final Channel ch;
    final Packet1Login forgeLogin;
    final List<PacketFAPluginMessage> loginMessages;
    @Getter
    private final PendingConnection pendingConnection;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private ServerConnection server;
    // reconnect stuff
    public int clientEntityId;
    public int serverEntityId;
    // ping stuff
    public int trackingPingId;
    public long pingTime;
    @Getter
    private String name;
    @Getter
    private String displayName;
    @Getter
    @Setter
    private int ping = 1000;
    // Permissions
    private final Collection<String> playerGroups = new THashSet<>();
    private final Collection<String> permissions = new THashSet<>();
    private final Object permMutex = new Object();
    @Getter
    private final Object switchMutex = new Object();
    public PacketCCSettings settings;

    public UserConnection(BungeeCord bungee, Channel channel, PendingConnection pendingConnection, Packet2Handshake handshake, Packet1Login forgeLogin, List<PacketFAPluginMessage> loginMessages)
    {
        this.bungee = bungee;
        this.ch = channel;
        this.handshake = handshake;
        this.pendingConnection = pendingConnection;
        this.forgeLogin = forgeLogin;
        this.loginMessages = loginMessages;
        this.name = handshake.username;
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

    @Override
    public void setDisplayName(String name)
    {
        Preconditions.checkArgument( name.length() <= 16, "Display name cannot be longer than 16 characters" );
        bungee.getTabListHandler().onDisconnect( this );
        bungee.getTabListHandler().onConnect( this );
    }

    @Override
    public void connect(ServerInfo target)
    {
        connect( target, false );
    }

    public void connect(ServerInfo info, final boolean retry)
    {
        ServerConnectEvent event = new ServerConnectEvent( this, info );
        ProxyServer.getInstance().getPluginManager().callEvent( event );
        final ServerInfo target = event.getTarget(); // Update in case the event changed target
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
                    ServerInfo def = ProxyServer.getInstance().getServers().get( getPendingConnection().getListener().getDefaultServer() );
                    if ( retry && !target.equals( def ) )
                    {
                        sendMessage( ChatColor.RED + "Could not connect to target server, you have been moved to the default server" );
                        connect( def, false );
                    } else
                    {
                        if ( server == null )
                        {
                            disconnect( "Server down, could not connect to default! " + future.cause().getClass().getName() );
                        } else
                        {
                            sendMessage( ChatColor.RED + "Could not connect to target server: " + future.cause().getClass().getName() );
                        }
                    }
                }
            }
        } );
    }

    @Override
    public synchronized void disconnect(String reason)
    {
        if ( ch.isActive() )
        {
            bungee.getLogger().log( Level.INFO, "[" + getName() + "] disconnected with: " + reason );
            ch.write( new PacketFFKick( reason ) );
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
        server.getCh().write( new Packet3Chat( message ) );
    }

    @Override
    public void sendMessage(String message)
    {
        ch.write( new Packet3Chat( message ) );
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        ch.write( new PacketFAPluginMessage( channel, data ) );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) ch.remoteAddress();
    }

    @Override
    @Synchronized("permMutex")
    public Collection<String> getGroups()
    {
        return Collections.unmodifiableCollection( playerGroups );
    }

    @Override
    @Synchronized("permMutex")
    public void addGroups(String... groups)
    {
        for ( String group : groups )
        {
            playerGroups.add( group );
            for ( String permission : bungee.getConfigurationAdapter().getPermissions( group ) )
            {
                setPermission( permission, true );
            }
        }
    }

    @Override
    @Synchronized("permMutex")
    public void removeGroups(String... groups)
    {
        for ( String group : groups )
        {
            playerGroups.remove( group );
            for ( String permission : bungee.getConfigurationAdapter().getPermissions( group ) )
            {
                setPermission( permission, false );
            }
        }
    }

    @Override
    @Synchronized("permMutex")
    public boolean hasPermission(String permission)
    {
        return permissions.contains( permission );
    }

    @Override
    @Synchronized("permMutex")
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
}
