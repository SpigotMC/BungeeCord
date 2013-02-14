package net.md_5.bungee;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketStream;
import net.md_5.mendax.PacketDefinitions;

/**
 * Class representing a connection from the proxy to the server; ie upstream.
 */
public class ServerConnection extends GenericConnection implements Server
{

    @Getter
    private final ServerInfo info;
    public final Packet1Login loginPacket;
    public Queue<DefinedPacket> packetQueue = new ConcurrentLinkedQueue<>();

    public ServerConnection(Socket socket, ServerInfo info, PacketStream stream, Packet1Login loginPacket)
    {
        super( socket, stream );
        this.info = info;
        this.loginPacket = loginPacket;
    }

    public static ServerConnection connect(UserConnection user, ServerInfo info, Packet2Handshake handshake, boolean retry)
    {
        try
        {
            Socket socket = new Socket();
            socket.connect( info.getAddress(), BungeeCord.getInstance().config.getTimeout() );
            BungeeCord.getInstance().setSocketOptions( socket );

            PacketStream stream = new PacketStream( socket.getInputStream(), socket.getOutputStream(), user.stream.getProtocol() );

            if ( user.forgeLogin != null )
            {
                stream.write( user.forgeLogin );
            }

            stream.write( handshake );
            stream.write( PacketCDClientStatus.CLIENT_LOGIN );
            stream.readPacket();

            byte[] loginResponse = null;
            loop:
            while ( true )
            {
                loginResponse = stream.readPacket();
                int id = Util.getId( loginResponse );
                switch ( id )
                {
                    case 0x01:
                        break loop;
                    case 0xFA:
                        for ( PacketFAPluginMessage message : user.loginMessages )
                        {
                            stream.write( message );
                        }
                        break;
                    case 0xFF:
                        throw new KickException( "[Kicked] " + new PacketFFKick( loginResponse ).message );
                    default:
                        throw new IllegalArgumentException( "Unknown login packet " + Util.hex( id ) );
                }
            }
            Packet1Login login = new Packet1Login( loginResponse );

            ServerConnection server = new ServerConnection( socket, info, stream, login );
            ServerConnectedEvent event = new ServerConnectedEvent( user, server );
            ProxyServer.getInstance().getPluginManager().callEvent( event );

            stream.write( BungeeCord.getInstance().registerChannels() );

            Queue<DefinedPacket> packetQueue = ( (BungeeServerInfo) info ).getPacketQueue();
            while ( !packetQueue.isEmpty() )
            {
                stream.write( packetQueue.poll() );
            }

            return server;
        } catch ( KickException ex )
        {
            throw ex;
        } catch ( Exception ex )
        {
            ServerInfo def = ProxyServer.getInstance().getServers().get( user.getPendingConnection().getListener().getDefaultServer() );
            if ( retry && !info.equals( def ) )
            {
                user.sendMessage( ChatColor.RED + "Could not connect to target server, you have been moved to the default server" );
                return connect( user, def, handshake, false );
            } else
            {
                throw new RuntimeException( "Could not connect to target server " + Util.exception( ex ) );
            }
        }
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        packetQueue.add( new PacketFAPluginMessage( channel, data ) );
    }

    @Override
    public void ping(final Callback<ServerPing> callback)
    {
        getInfo().ping( callback );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return getInfo().getAddress();
    }
}
