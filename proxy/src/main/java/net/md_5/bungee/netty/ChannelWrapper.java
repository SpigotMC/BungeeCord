package net.md_5.bungee.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import lombok.Getter;

public class ChannelWrapper
{

    private final Channel ch;
    @Getter
    private volatile boolean closed;
    private final MessageList<Object> queue = MessageList.newInstance();
    private volatile boolean flushNow = true;

    public ChannelWrapper(ChannelHandlerContext ctx)
    {
        this.ch = ctx.channel();
    }

    public synchronized void flushNow(boolean flush)
    {
        if ( !flushNow && flush )
        {
            ch.write( queue.copy() );
            queue.clear();
        }
        this.flushNow = flush;
    }

    public synchronized void write(Object packet)
    {
        if ( !closed )
        {
            if ( flushNow )
            {
                ch.write( packet );
            } else
            {
                queue.add( packet );
            }
        }
    }

    public synchronized void close()
    {
        if ( !closed )
        {
            closed = true;
            ch.write( queue );
            ch.close();
        }
    }

    public Channel getHandle()
    {
        return ch;
    }
}
