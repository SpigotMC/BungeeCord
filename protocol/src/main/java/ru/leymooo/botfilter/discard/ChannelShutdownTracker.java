package ru.leymooo.botfilter.discard;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class ChannelShutdownTracker
{
    private final Channel ch;
    private boolean shutdown;

    public ChannelFuture shutdown(ChannelHandlerContext ctx)
    {
        this.shutdown = true;
        val ch = this.ch;
        ch.pipeline().addFirst( ChannelDiscardHandler.DISCARD_FIRST, ChannelDiscardHandler.INSTANCE )
            .addAfter( ctx.name(), ChannelDiscardHandler.DISCARD, ChannelDiscardHandler.INSTANCE );
        return ch.close();
    }

    public ChannelFuture shutdown(String ctx)
    {
        this.shutdown = true;
        val ch = this.ch;
        ch.pipeline().addFirst( ChannelDiscardHandler.DISCARD_FIRST, ChannelDiscardHandler.INSTANCE )
            .addAfter( ctx, ChannelDiscardHandler.DISCARD, ChannelDiscardHandler.INSTANCE );
        return ch.close();
    }


    public boolean isShuttedDown()
    {
        return this.shutdown;
    }
}
