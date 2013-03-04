package net.md_5.bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.Synchronized;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.packet.*;

public final class UserConnection extends GenericConnection implements ProxiedPlayer
{

    public final Packet2Handshake handshake;
    final Packet1Login forgeLogin;
    final List<PacketFAPluginMessage> loginMessages;
    public Queue<DefinedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    @Getter
    private final PendingConnection pendingConnection;
    @Getter
    private ServerConnection server;
    private UpstreamBridge upBridge;
    private DownstreamBridge downBridge;
    // reconnect stuff
    private int clientEntityId;
    private int serverEntityId;
    private volatile boolean reconnecting;
    // ping stuff
    private int trackingPingId;
    private long pingTime;
    @Getter
    private int ping = 1000;
    // Permissions
    private final Collection<String> groups = new HashSet<>();
    private final Map<String, Boolean> permissions = new HashMap<>();
    private final Object permMutex = new Object();
    // Hack for connect timings
    private ServerInfo nextServer;
    private volatile boolean clientConnected = true;

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
        try
        {
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
            downBridge = new DownstreamBridge();
            downBridge.start();
        } catch ( KickException ex )
        {
            disconnect( ex.getMessage() );
        } catch ( Exception ex )
        {
            disconnect( "Could not connect to server - " + Util.exception( ex ) );
        }
    }

    @Override
    public synchronized void disconnect(String reason)
    {
        if ( clientConnected )
        {
            PlayerDisconnectEvent event = new PlayerDisconnectEvent( this );
            ProxyServer.getInstance().getPluginManager().callEvent( event );
            ProxyServer.getInstance().getTabListHandler().onDisconnect( this );
            ProxyServer.getInstance().getPlayers().remove( this );

            super.disconnect( reason );
            if ( server != null )
            {
                server.getInfo().removePlayer( this );
                server.disconnect( "Quitting" );
                ProxyServer.getInstance().getReconnectHandler().setServer( this );
            }

            clientConnected = false;
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
        server.packetQueue.add( new PacketFAPluginMessage( channel, data ) );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) socket.getRemoteSocketAddress();
    }

    @Override
    @Synchronized("permMutex")
    public Collection<String> getGroups()
    {
        return Collections.unmodifiableCollection( groups );
    }

    @Override
    @Synchronized("permMutex")
    public void addGroups(String... groups)
    {
        for ( String group : groups )
        {
            this.groups.add( group );
            for ( String permission : ProxyServer.getInstance().getConfigurationAdapter().getPermissions( group ) )
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
            this.groups.remove( group );
            for ( String permission : ProxyServer.getInstance().getConfigurationAdapter().getPermissions( group ) )
            {
                setPermission( permission, false );
            }
        }
    }

    @Override
    @Synchronized("permMutex")
    public boolean hasPermission(String permission)
    {
        Boolean val = permissions.get( permission );
        return ( val == null ) ? false : val;
    }

    @Override
    @Synchronized("permMutex")
    public void setPermission(String permission, boolean value)
    {
        permissions.put( permission, value );
    }

    private class UpstreamBridge extends Thread
    {

        public UpstreamBridge()
        {
            super( "Upstream Bridge - " + name );
        }

        @Override
        public void run()
        {
            while ( !socket.isClosed() )
            {
                try
                {
                    byte[] packet = stream.readPacket();
                    boolean sendPacket = true;
                    int id = Util.getId( packet );

                    switch ( id )
                    {
                        case 0x00:
                            if ( trackingPingId == new Packet0KeepAlive( packet ).id )
                            {
                                int newPing = (int) ( System.currentTimeMillis() - pingTime );
                                ProxyServer.getInstance().getTabListHandler().onPingChange( UserConnection.this, newPing );
                                ping = newPing;
                            }
                            break;
                        case 0x03:
                            Packet3Chat chat = new Packet3Chat( packet );
                            if ( chat.message.startsWith( "/" ) )
                            {
                                sendPacket = !ProxyServer.getInstance().getPluginManager().dispatchCommand( UserConnection.this, chat.message.substring( 1 ) );
                            } else
                            {
                                ChatEvent chatEvent = new ChatEvent( UserConnection.this, server, chat.message );
                                ProxyServer.getInstance().getPluginManager().callEvent( chatEvent );
                                sendPacket = !chatEvent.isCancelled();
                            }
                            break;
                        case 0xFA:
                            // Call the onPluginMessage event
                            PacketFAPluginMessage message = new PacketFAPluginMessage( packet );

                            // Might matter in the future
                            if ( message.tag.equals( "BungeeCord" ) )
                            {
                                continue;
                            }

                            PluginMessageEvent event = new PluginMessageEvent( UserConnection.this, server, message.tag, message.data );
                            ProxyServer.getInstance().getPluginManager().callEvent( event );

                            if ( event.isCancelled() )
                            {
                                continue;
                            }
                            break;
                    }

                    while ( !server.packetQueue.isEmpty() )
                    {
                        DefinedPacket p = server.packetQueue.poll();
                        if ( p != null )
                        {
                            server.stream.write( p );
                        }
                    }

                    EntityMap.rewrite( packet, clientEntityId, serverEntityId );
                    if ( sendPacket && !server.socket.isClosed() )
                    {
                        server.stream.write( packet );
                    }

                    try
                    {
                        Thread.sleep( BungeeCord.getInstance().config.getSleepTime() );
                    } catch ( InterruptedException ex )
                    {
                    }
                } catch ( IOException ex )
                {
                    disconnect( "Reached end of stream" );
                } catch ( Exception ex )
                {
                    disconnect( Util.exception( ex ) );
                }
            }
        }
    }

    private class DownstreamBridge extends Thread
    {

        public DownstreamBridge()
        {
            super( "Downstream Bridge - " + name );
        }

        @Override
        public void run()
        {
            try
            {
                outer:
                while ( !reconnecting )
                {
                    byte[] packet = server.stream.readPacket();
                    int id = Util.getId( packet );

                    switch ( id )
                    {
                        case 0x00:
                            trackingPingId = new Packet0KeepAlive( packet ).id;
                            pingTime = System.currentTimeMillis();
                            break;
                        case 0x03:
                            Packet3Chat chat = new Packet3Chat( packet );
                            ChatEvent chatEvent = new ChatEvent( server, UserConnection.this, chat.message );
                            ProxyServer.getInstance().getPluginManager().callEvent( chatEvent );

                            if ( chatEvent.isCancelled() )
                            {
                                continue;
                            }
                            break;
                        case 0xC9:
                            PacketC9PlayerListItem playerList = new PacketC9PlayerListItem( packet );
                            if ( !ProxyServer.getInstance().getTabListHandler().onListUpdate( UserConnection.this, playerList.username, playerList.online, playerList.ping ) )
                            {
                                continue;
                            }
                            break;
                        case 0xFA:
                            // Call the onPluginMessage event
                            PacketFAPluginMessage message = new PacketFAPluginMessage( packet );
                            DataInputStream in = new DataInputStream( new ByteArrayInputStream( message.data ) );
                            PluginMessageEvent event = new PluginMessageEvent( server, UserConnection.this, message.tag, message.data );
                            ProxyServer.getInstance().getPluginManager().callEvent( event );

                            if ( event.isCancelled() )
                            {
                                continue;
                            }

                            if ( message.tag.equals( "BungeeCord" ) )
                            {
                                String subChannel = in.readUTF();
                                if ( subChannel.equals( "Forward" ) )
                                {
                                    String target = in.readUTF();
                                    String channel = in.readUTF();
                                    short len = in.readShort();
                                    byte[] data = new byte[ len ];
                                    in.readFully( data );


                                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                                    DataOutputStream out = new DataOutputStream( b );
                                    out.writeUTF( channel );
                                    out.writeShort( data.length );
                                    out.write( data );

                                    if ( target.equals( "ALL" ) )
                                    {
                                        for ( ServerInfo server : BungeeCord.getInstance().getServers().values() )
                                        {
                                            server.sendData( "BungeeCord", b.toByteArray() );
                                        }
                                    } else
                                    {
                                        ServerInfo server = BungeeCord.getInstance().getServerInfo( target );
                                        if ( server != null )
                                        {
                                            server.sendData( "BungeeCord", b.toByteArray() );
                                        }
                                    }
                                }
                                if ( subChannel.equals( "Connect" ) )
                                {
                                    ServerInfo server = ProxyServer.getInstance().getServerInfo( in.readUTF() );
                                    if ( server != null )
                                    {
                                        connect( server, true );
                                        break outer;
                                    }
                                }
                                if ( subChannel.equals( "IP" ) )
                                {
                                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                                    DataOutputStream out = new DataOutputStream( b );
                                    out.writeUTF( "IP" );
                                    out.writeUTF( getAddress().getHostString() );
                                    out.writeInt( getAddress().getPort() );
                                    getServer().sendData( "BungeeCord", b.toByteArray() );
                                }
                                if ( subChannel.equals( "PlayerCount" ) )
                                {
                                    ServerInfo server = ProxyServer.getInstance().getServerInfo( in.readUTF() );
                                    if ( server != null )
                                    {
                                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                                        DataOutputStream out = new DataOutputStream( b );
                                        out.writeUTF( "PlayerCount" );
                                        out.writeUTF( server.getName() );
                                        out.writeInt( server.getPlayers().size() );
                                        getServer().sendData( "BungeeCord", b.toByteArray() );
                                    }
                                }
                                if ( subChannel.equals( "PlayerList" ) )
                                {
                                    ServerInfo server = ProxyServer.getInstance().getServerInfo( in.readUTF() );
                                    if ( server != null )
                                    {
                                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                                        DataOutputStream out = new DataOutputStream( b );
                                        out.writeUTF( "PlayerList" );
                                        out.writeUTF( server.getName() );

                                        StringBuilder sb = new StringBuilder();
                                        for ( ProxiedPlayer p : server.getPlayers() )
                                        {
                                            sb.append( p.getName() );
                                            sb.append( "," );
                                        }
                                        out.writeUTF( sb.substring( 0, sb.length() - 1 ) );

                                        getServer().sendData( "BungeeCord", b.toByteArray() );
                                    }
                                }
                                if ( subChannel.equals( "GetServers" ) )
                                {
                                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                                    DataOutputStream out = new DataOutputStream( b );
                                    out.writeUTF( "GetServers" );

                                    StringBuilder sb = new StringBuilder();
                                    for ( String server : ProxyServer.getInstance().getServers().keySet() )
                                    {
                                        sb.append( server );
                                        sb.append( "," );
                                    }
                                    out.writeUTF( sb.substring( 0, sb.length() - 1 ) );

                                    getServer().sendData( "BungeeCord", b.toByteArray() );
                                }
                                if ( subChannel.equals( "Message" ) )
                                {
                                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer( in.readUTF() );
                                    if ( target != null )
                                    {
                                        target.sendMessage( in.readUTF() );
                                    }
                                }
                                continue;
                            }
                            break;
                        case 0xFF:
                            disconnect( new PacketFFKick( packet ).message );
                            break outer;
                    }

                    while ( !packetQueue.isEmpty() )
                    {
                        DefinedPacket p = packetQueue.poll();
                        if ( p != null )
                        {
                            stream.write( p );
                        }
                    }

                    EntityMap.rewrite( packet, serverEntityId, clientEntityId );
                    stream.write( packet );

                    if ( nextServer != null )
                    {
                        connect( nextServer, true );
                        break outer;
                    }
                }
            } catch ( Exception ex )
            {
                disconnect( Util.exception( ex ) );
            }
        }
    }
}
