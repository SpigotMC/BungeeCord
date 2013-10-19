package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import net.md_5.bungee.protocol.packet.LegacyPing;

public class LegacyDecoder extends ByteToMessageDecoder
{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if ( in.readableBytes() < 3 )
        {
            return;
        }
        int i = in.readerIndex();
        short b1 = in.getUnsignedByte( i++ );
        short b2 = in.getUnsignedByte( i++ );
        short b3 = in.getUnsignedByte( i++ );

        if ( b1 == 0xFE && b2 == 0x01 && b3 == 0xFA )
        {
            out.add( new PacketWrapper( new LegacyPing(), Unpooled.EMPTY_BUFFER ) );
        }
        ctx.pipeline().remove( this );
    }
}
