package net.md_5.bungee;

import java.net.InetSocketAddress;
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
    }
}
