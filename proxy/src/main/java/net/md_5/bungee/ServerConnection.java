package net.md_5.bungee;

import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFFKick;

@RequiredArgsConstructor
public class ServerConnection implements Server
{

    @Getter
    private final ChannelWrapper ch;
    @Getter
    private final BungeeServerInfo info;
    @Getter
    private final Packet1Login loginPacket;
    @Getter
    @Setter
    private boolean isObsolete;

    @Override
    public void sendData(String channel, byte[] data)
    {
        ch.write( new PacketFAPluginMessage( channel, data ) );
    }

    @Override
    public synchronized void disconnect(String reason)
    {
        if ( ch.getHandle().isActive() )
        {
            ch.write( new PacketFFKick( reason ) );
            ch.getHandle().eventLoop().schedule( new Runnable()
            {
                @Override
                public void run()
                {
                    ch.getHandle().close();
                }
            }, 100, TimeUnit.MILLISECONDS );
        }
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return getInfo().getAddress();
    }
}
