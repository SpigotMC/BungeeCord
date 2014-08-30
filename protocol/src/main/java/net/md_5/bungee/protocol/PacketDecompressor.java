package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.zip.Inflater;

public class PacketDecompressor extends ByteToMessageDecoder
{

    private final Inflater inflater = new Inflater();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if ( in.readableBytes() == 0 )
        {
            return;
        }

        int size = DefinedPacket.readVarInt( in );
        if ( size == 0 )
        {
            out.add( in.readBytes( in.readableBytes() ) );
        } else
        {
            byte[] compressedData = new byte[ in.readableBytes() ];
            in.readBytes( compressedData );
            inflater.setInput( compressedData );

            byte[] data = new byte[ size ];
            inflater.inflate( data );
            out.add( Unpooled.wrappedBuffer( data ) );
            inflater.reset();
        }
    }
}
