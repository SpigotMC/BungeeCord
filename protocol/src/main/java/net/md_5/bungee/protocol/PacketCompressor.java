package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Setter;

import java.util.zip.Deflater;

public class PacketCompressor extends MessageToByteEncoder<ByteBuf>
{

    private final byte[] buffer = new byte[ 8192 ];
    private final Deflater deflater = new Deflater();
    @Setter
    private int threshold = 256;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception
    {
        int origSize = msg.readableBytes();
        if ( origSize < threshold )
        {
            DefinedPacket.writeVarInt( 0, out );
            out.writeBytes( msg );
        } else
        {
            byte[] data = new byte[ origSize ];
            msg.readBytes( data );

            DefinedPacket.writeVarInt( data.length, out );

            deflater.setInput( data );
            deflater.finish();
            while ( !deflater.finished() )
            {
                int count = deflater.deflate( buffer );
                out.writeBytes( buffer, 0, count );
            }
            deflater.reset();
        }
    }
}
