package net.md_5.bungee;

import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketStream;
import net.md_5.bungee.protocol.PacketDefinitions;

public class BungeeServerInfo extends ServerInfo
{

    @Getter
    private final Queue<DefinedPacket> packetQueue = new ConcurrentLinkedQueue<>();

    public BungeeServerInfo(String name, InetSocketAddress address)
    {
        super( name, address );
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        Server server = ProxyServer.getInstance().getServer( getName() );
        if ( server != null )
        {
            server.sendData( channel, data );
        } else
        {
            packetQueue.add( new PacketFAPluginMessage( channel, data ) );
        }
    }

    @Override
    public void ping(final Callback<ServerPing> callback)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try ( Socket socket = new Socket(); )
                {
                    socket.connect( getAddress() );

                    DataOutputStream out = new DataOutputStream( socket.getOutputStream() );
                    out.write( 0xFE );
                    out.write( 0x01 );

                    PacketStream in = new PacketStream( socket.getInputStream(), PacketDefinitions.VANILLA_PROTOCOL );
                    PacketFFKick response = new PacketFFKick( in.readPacket() );

                    String[] split = response.message.split( "\00" );

                    ServerPing ping = new ServerPing( Byte.parseByte( split[1] ), split[2], split[3], Integer.parseInt( split[4] ), Integer.parseInt( split[5] ) );
                    callback.done( ping, null );
                } catch ( Throwable t )
                {
                    callback.done( null, t );
                }
            }
        }.start();
    }
}
