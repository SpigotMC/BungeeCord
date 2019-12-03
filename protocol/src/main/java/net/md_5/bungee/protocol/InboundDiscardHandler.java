package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@ChannelHandler.Sharable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InboundDiscardHandler extends ChannelInboundHandlerAdapter
{
    public static final String DISCARD_FIRST = "I_DISCARD_FIRST";
    public static final String DISCARD = "I_DISCARD";

    public static InboundDiscardHandler INSTANCE = new InboundDiscardHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        if ( msg instanceof ByteBuf )
        {
            ( (ByteBuf) msg ).release();
            ctx.close();
        }
    }
}
