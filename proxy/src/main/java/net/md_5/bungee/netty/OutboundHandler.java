package net.md_5.bungee.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOperationHandlerAdapter;
import io.netty.channel.ChannelPromise;
import java.nio.channels.ClosedChannelException;

public class OutboundHandler extends ChannelOperationHandlerAdapter
{

    @Override
    public void flush(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception
    {
        ctx.flush( promise );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        if ( !( cause instanceof ClosedChannelException ) )
        {
            ctx.fireExceptionCaught( cause );
        }
    }
}
