package net.md_5.bungee.util;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import net.md_5.bungee.error.Errors;

@ChannelHandler.Sharable
final class ChannelDiscardHandler extends ChannelOutboundHandlerAdapter
{
    static final ChannelDiscardHandler INSTANCE = new ChannelDiscardHandler();

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception
    {
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
    {
        ReferenceCountUtil.release( msg );
        promise.setFailure( Errors.discard() );
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception
    {
    }
}
