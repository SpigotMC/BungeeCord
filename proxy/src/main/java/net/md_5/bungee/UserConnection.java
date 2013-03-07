package net.md_5.bungee;

import gnu.trove.set.hash.THashSet;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.packet.*;

public final class UserConnection implements ProxiedPlayer
{

    public final Packet2Handshake handshake;
    private final ProxyServer bungee;
    private final Channel ch;
    final Packet1Login forgeLogin;
    final List<PacketFAPluginMessage> loginMessages;
    public Queue<DefinedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    @Getter
    private final PendingConnection pendingConnection;
    @Getter
    private ServerConnection server;
    // reconnect stuff
    private int clientEntityId;
    private int serverEntityId;
    // ping stuff
    public int trackingPingId;
    public long pingTime;
    @Getter
    @Setter
    private int ping = 1000;
    // Permissions
    private final Collection<String> playerGroups = new HashSet<>();
    private final THashSet<String> permissions = new THashSet<>();
    private final Object permMutex = new Object();

    public UserConnection(Socket socket, PendingConnection pendingConnection, PacketStream stream, Packet2Handshake handshake, Packet1Login forgeLogin, List<PacketFAPluginMessage> loginMessages)
    {
        super( socket, stream );
        this.handshake = handshake;
        this.pendingConnection = pendingConnection;
        this.forgeLogin = forgeLogin;
        this.loginMessages = loginMessages;
        name = handshake.username.substring( 0, Math.min( handshake.username.length(), 16 ) );
        displayName = name;

        Collection<String> g = ProxyServer.getInstance().getConfigurationAdapter().getGroups( name );
        for ( String s : g )
        {
            addGroups( s );
        }
    }

    @Override
    public void setDisplayName(String name)
    {
        ProxyServer.getInstance().getTabListHandler().onDisconnect( this );
        displayName = name;
        ProxyServer.getInstance().getTabListHandler().onConnect( this );
    }

    @Override
    public void connect(ServerInfo target)
    {
        nextServer = target;
    }

    public void connect(ServerInfo target, boolean force)
    {
        nextServer = null;
        if ( server == null )
        {
            // First join
            BungeeCord.getInstance().connections.put( name, this );
            ProxyServer.getInstance().getTabListHandler().onConnect( this );
        }

        ServerConnectEvent event = new ServerConnectEvent( this, target );
        BungeeCord.getInstance().getPluginManager().callEvent( event );
        target = event.getTarget(); // Update in case the event changed target

        ProxyServer.getInstance().getTabListHandler().onServerChange( this );

        reconnecting = true;

        if ( server != null )
        {
            stream.write( new Packet9Respawn( (byte) 1, (byte) 0, (byte) 0, (short) 256, "DEFAULT" ) );
            stream.write( new Packet9Respawn( (byte) -1, (byte) 0, (byte) 0, (short) 256, "DEFAULT" ) );
        }

        ServerConnection newServer = ServerConnector.connect( this, target, true );
        if ( server == null )
        {
            // Once again, first connection
            clientEntityId = newServer.loginPacket.entityId;
            serverEntityId = newServer.loginPacket.entityId;
            // Set tab list size
            Packet1Login s = newServer.loginPacket;
            Packet1Login login = new Packet1Login( s.entityId, s.levelType, s.gameMode, (byte) s.dimension, s.difficulty, s.unused, (byte) pendingConnection.getListener().getTabListSize() );
            stream.write( login );
            stream.write( BungeeCord.getInstance().registerChannels() );

            upBridge = new UpstreamBridge();
            upBridge.start();
        } else
        {
            try
            {
                downBridge.interrupt();
                downBridge.join();
            } catch ( InterruptedException ie )
            {
            }

            server.disconnect( "Quitting" );
            server.getInfo().removePlayer( this );

            Packet1Login login = newServer.loginPacket;
            serverEntityId = login.entityId;
            stream.write( new Packet9Respawn( login.dimension, login.difficulty, login.gameMode, (short) 256, login.levelType ) );
        }

        // Reconnect process has finished, lets get the player moving again
        reconnecting = false;

        // Add to new
        target.addPlayer( this );

        // Start the bridges and move on
        server = newServer;
    }

    @Override
    public synchronized void disconnect(String reason)
    {
        if ( ch.isActive() )
        {
            PlayerDisconnectEvent event = new PlayerDisconnectEvent( this );
            bungee.getPluginManager().callEvent( event );
            bungee.getTabListHandler().onDisconnect( this );
            bungee.getPlayers().remove( this );

            ch.write( new PacketFFKick( reason ) );
            ch.close();

            if ( server != null )
            {
                server.getInfo().removePlayer( this );
                server.disconnect( "Quitting" );
                bungee.getReconnectHandler().setServer( this );
            }
        }
    }

    @Override
    public void sendMessage(String message)
    {
        packetQueue.add( new Packet3Chat( message ) );
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
