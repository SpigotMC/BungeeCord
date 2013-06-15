package net.md_5.bungee.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import java.nio.channels.ClosedChannelException;

public class OutboundBoss extends ChannelOutboundHandlerAdapter
{

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        if ( !( cause instanceof ClosedChannelException ) )
        {
            ctx.fireExceptionCaught( cause );
        }
    }
}
