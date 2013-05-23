package net.md_5.bungee.netty;

import io.netty.channel.Channel;
import lombok.Getter;

public class ChannelWrapper
{

    private final Channel ch;
    @Getter
    private volatile boolean closed;

    public ChannelWrapper(Channel ch)
    {
        this.ch = ch;
    }

    public synchronized void write(Object packet)
    {
        if ( !closed )
        {
            ch.write( packet, ch.voidPromise() );
        }
    }

    public synchronized void close()
    {
        if ( !closed )
        {
            closed = true;
            ch.close();
        }
    }

    public Channel getHandle()
    {
        return ch;
    }
}
