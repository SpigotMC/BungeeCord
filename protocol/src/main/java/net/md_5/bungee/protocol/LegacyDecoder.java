package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import net.md_5.bungee.protocol.packet.LegacyHandshake;
import net.md_5.bungee.protocol.packet.LegacyPing;

public class LegacyDecoder extends ByteToMessageDecoder
{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        // See check in Varint21FrameDecoder for more details
        if ( !ctx.channel().isActive() )
        {
            in.skipBytes( in.readableBytes() );
            return;
        }

        if ( !in.isReadable() )
        {
            return;
        }

        in.markReaderIndex();
        short packetID = in.readUnsignedByte();

        if ( packetID == 0xFE )
        {
            out.add( new PacketWrapper( new LegacyPing( in.isReadable() && in.readUnsignedByte() == 0x01 ), Unpooled.EMPTY_BUFFER ) );
            return;
        } else if ( packetID == 0x02 && in.isReadable() )
        {
            in.skipBytes( in.readableBytes() );
            out.add( new PacketWrapper( new LegacyHandshake(), Unpooled.EMPTY_BUFFER ) );
            return;
        }

        in.resetReaderIndex();
        ctx.pipeline().remove( this );
    }
}
