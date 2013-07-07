package net.md_5.bungee;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.DefinedPacket;
import net.md_5.bungee.protocol.packet.PacketFAPluginMessage;
import net.md_5.bungee.protocol.packet.PacketFFKick;

@RequiredArgsConstructor
public class ServerConnection implements Server
{

    @Getter
    private final ChannelWrapper ch;
    @Getter
    private final BungeeServerInfo info;
    @Getter
    @Setter
    private boolean isObsolete;
    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }
    };

    @Override
    public void sendData(String channel, byte[] data)
    {
        unsafe().sendPacket( new PacketFAPluginMessage( channel, data ) );
    }

    @Override
    public synchronized void disconnect(String reason)
    {
        if ( !ch.isClosed() )
        {
            // TODO: Can we just use a future here?
            unsafe().sendPacket( new PacketFFKick( reason ) );
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

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }
}
