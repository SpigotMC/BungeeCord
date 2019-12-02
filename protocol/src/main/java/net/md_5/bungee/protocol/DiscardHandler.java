package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@ChannelHandler.Sharable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscardHandler extends ChannelInboundHandlerAdapter
{
    static final String DISCARD_FIRST = "DISCARD_FIRST";
    static final String DISCARD = "DISCARD";

    static DiscardHandler INSTANCE = new DiscardHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        ( (ByteBuf) msg ).release();
        ctx.close();
    }
}
