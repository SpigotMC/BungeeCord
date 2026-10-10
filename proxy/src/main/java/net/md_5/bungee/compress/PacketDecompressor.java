package net.md_5.bungee.compress;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.OverflowPacketException;

public class PacketDecompressor extends MessageToMessageDecoder<ByteBuf>
{

    private static final int MAX_DECOMPRESSED_LEN = 1 << 23;
    private final Inflater zlib = new Inflater();

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        zlib.end();
    }

    private void process(ByteBuf in, ByteBuf out) throws DataFormatException
    {
        int buffersInIdx = 0;
        ByteBuffer[] buffersIn = in.nioBuffers();

        int buffersOutIdx = 0;
        ByteBuffer[] buffersOut = null;

        zlib.setInput( buffersIn[buffersInIdx] );

        while ( !zlib.finished() )
        {
            Preconditions.checkState( out.isWritable(), "Output buffer not writable. Overflow?" );

            if ( buffersOut == null )
            {
                buffersOutIdx = 0;
                buffersOut = out.nioBuffers( out.writerIndex(), out.writableBytes() );
            }

            int totalIn = zlib.getTotalIn();
            ByteBuffer bufferOut = buffersOut[buffersOutIdx];
            int written = zlib.inflate( bufferOut );
            int read = zlib.getTotalIn() - totalIn;

            in.readerIndex( in.readerIndex() + read );
            out.writerIndex( out.writerIndex() + written );

            if ( !bufferOut.hasRemaining() )
            {
                buffersOutIdx++;
            }

            if ( written == 0 )
            {
                if ( zlib.needsInput() )
                {
                    zlib.setInput( buffersIn[++buffersInIdx] );
                } else
                {
                    throw new IllegalStateException( "No bytes written but no input required" );
                }
            }
        }

        zlib.reset();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        int size = DefinedPacket.readVarInt( in );
        if ( size == 0 )
        {
            out.add( in.retain() );
        } else
        {
            if ( size > MAX_DECOMPRESSED_LEN )
            {
                throw new OverflowPacketException( "Packet may not be larger than " + MAX_DECOMPRESSED_LEN + " bytes" );
            }

            // Do not use size as max capacity, as its possible that the entity rewriter increases the size afterwards
            // This would result in a kick (it happens rarely as the entity ids size must differ)
            ByteBuf decompressed = ctx.alloc().directBuffer( size, MAX_DECOMPRESSED_LEN );
            try
            {
                process( in, decompressed );
                Preconditions.checkState( decompressed.readableBytes() == size, "Decompressed packet size mismatch" );

                out.add( decompressed );
                decompressed = null;
            } finally
            {
                if ( decompressed != null )
                {
                    decompressed.release();
                }
            }
        }
    }
}
