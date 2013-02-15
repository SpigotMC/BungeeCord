package net.md_5.bungee;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketStream;

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
