package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class HackEncoder extends MessageToByteEncoder<Wrapper>
{

    @Override
    protected void encode(ChannelHandlerContext ctx, Wrapper msg, ByteBuf out) throws Exception
    {
        out.capacity( msg.buf.readableBytes() );
        out.writeBytes( msg.buf );
        msg.buf.release();
    }
}
