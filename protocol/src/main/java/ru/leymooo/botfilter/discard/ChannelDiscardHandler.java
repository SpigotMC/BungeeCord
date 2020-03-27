package ru.leymooo.botfilter.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@ChannelHandler.Sharable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ChannelDiscardHandler extends ChannelInboundHandlerAdapter
{
    static final String DISCARD = "I_DISCARD";
    static final ChannelDiscardHandler INSTANCE = new ChannelDiscardHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        if ( msg instanceof ByteBuf )
        {
            ( (ByteBuf) msg ).release();
            Channel ch = ctx.channel();
            if ( ch.isActive() )
            {
                ch.close();
            }
        }
    }
}
