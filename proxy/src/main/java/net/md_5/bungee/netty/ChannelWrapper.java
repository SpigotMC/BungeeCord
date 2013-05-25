package net.md_5.bungee.netty;

import io.netty.channel.Channel;
import lombok.Getter;

public class ChannelWrapper
{

    private final Channel ch;
    @Getter
    private volatile boolean closed;
    private final ReusableChannelPromise promise;

    public ChannelWrapper(Channel ch)
    {
        this.ch = ch;
        this.promise = new ReusableChannelPromise( ch );
    }

    public synchronized void write(Object packet)
    {
        if ( !closed )
        {
            ch.write( packet, promise );
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
