package net.md_5.bungee;

import com.google.common.base.Preconditions;
import gnu.trove.set.hash.THashSet;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
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

    public UserConnection(Channel channel, PendingConnection pendingConnection, Packet2Handshake handshake, Packet1Login forgeLogin, List<PacketFAPluginMessage> loginMessages)
    {
        this.handshake = handshake;
        this.pendingConnection = pendingConnection;
        this.forgeLogin = forgeLogin;
        this.loginMessages = loginMessages;


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
