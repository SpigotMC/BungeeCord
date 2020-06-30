package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import net.md_5.bungee.error.Errors;

public class Varint21FrameDecoder extends ByteToMessageDecoder
{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        in.markReaderIndex();

        int i = 3;
        while ( i-- > 0 )
        {
            if ( !in.isReadable() )
            {
                in.resetReaderIndex();
                return;
            }

            byte read = in.readByte();
            if ( read >= 0 )
            {
                in.resetReaderIndex();
                int packetLength = DefinedPacket.readVarInt( in );

                if ( packetLength <= 0 )
                {
                    super.setSingleDecode( true );
                    Errors.emptyPacket();
                    return;
                }

                if ( in.readableBytes() < packetLength )
                {
                    in.resetReaderIndex();
                    return;
                }
                out.add( in.readRetainedSlice( packetLength ) );
                return;
            }
        }

        super.setSingleDecode( true );
        Errors.badFrameLength();
    }
}
