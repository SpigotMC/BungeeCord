package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public class Varint21FrameDecoder extends ByteToMessageDecoder
{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        in.markReaderIndex();

        final byte[] buf = new byte[ 3 ];
        for ( int i = 0; i < buf.length; i++ )
        {
            if ( !in.isReadable() )
            {
                in.resetReaderIndex();
                return;
            }

            buf[i] = in.readByte();
            if ( buf[i] >= 0 )
            {
                int length = DefinedPacket.readVarInt( Unpooled.wrappedBuffer( buf ) );
                if ( length == 0 )
                {
                    throw new CorruptedFrameException( "Empty Packet!" );
                }

                if ( in.readableBytes() < length )
                {
                    in.resetReaderIndex();
                    return;
                } else
                {
                    // TODO: Really should be a slice!
                    ByteBuf dst = ctx.alloc().directBuffer( length );
                    in.readBytes( dst );

                    out.add( dst );
                    return;
                }
            }
        }

        throw new CorruptedFrameException( "length wider than 21-bit" );
    }
}
