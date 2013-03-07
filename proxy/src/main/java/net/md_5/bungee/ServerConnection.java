package net.md_5.bungee;

import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.PacketFAPluginMessage;

@RequiredArgsConstructor
public class ServerConnection implements Server
{

    private final Channel ch;
    @Getter
    private final ServerInfo info;
    @Getter
    private final Packet1Login loginPacket;

    @Override
    public void sendData(String channel, byte[] data)
    {
        ch.write( new PacketFAPluginMessage( channel, data ) );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return getInfo().getAddress();
    }
}
