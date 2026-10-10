package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.Deflater;
import lombok.Setter;
import net.md_5.bungee.protocol.DefinedPacket;

/**
 * prepends length of message and optionally compresses message beforehand
 * <br>
 * combining these operations allows to keep space infront of compressed data
 * for length varint
 */
public class LengthPrependerAndCompressor extends MessageToMessageEncoder<ByteBuf>
{

    // reasonable to not support length varints > 4 byte (268435455 byte > 268MB)
    // if ever changed to smaller than 4, also change varintSize method to check for that
    private static final byte MAX_SUPPORTED_VARINT_LENGTH_LEN = 4;
    private static final byte FLAG_COMPRESS = 0x01;
    /**
     * overridden by FLAG_TWO_BUFFERS if set
     */
    private static final byte FLAG_COMPOSE = 0x02;
    /**
     * overwrites FLAG_COMPOSE if set
     */
    private static final byte FLAG_TWO_BUFFERS = 0x04;
    /**
     * minimum number of bytes compressed in a single compression iteration
     */
    private static final int COMPRESS_BUFFER_SIZE = 8192;

    public LengthPrependerAndCompressor(boolean compose, boolean twoBuffers)
    {
        setCompose( compose );
        setTwoBuffers( twoBuffers );
    }

    private Deflater zlib;
    @Setter
    private int threshold = 256;
    private byte flags = FLAG_COMPOSE;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception
    {
        int oldBodyLen = msg.readableBytes();
        final byte flags = this.flags;
        if ( ( flags & FLAG_COMPRESS ) != 0 )
        {
            if ( oldBodyLen < threshold )
            {
                byte lengthLen = varintSize( oldBodyLen + 1 );
                if ( ( flags & FLAG_TWO_BUFFERS ) != 0 )
                {
                    ByteBuf lenBuf = ctx.alloc().directBuffer( lengthLen );
                    DefinedPacket.writeVarInt( oldBodyLen + 1, lenBuf );
                    lenBuf.writeByte( 0 ); // indicates uncompressed
                    out.add( lenBuf );
                    out.add( msg.retain() );
                } else if ( ( flags & FLAG_COMPOSE ) != 0 )
                {
                    // create a virtual buffer to avoid copying of data
                    ByteBuf pre = ctx.alloc().directBuffer( lengthLen + 1 );
                    DefinedPacket.writeVarInt( oldBodyLen + 1, pre );
                    pre.writeByte( 0 ); // indicates uncompressed

                    out.add( ctx.alloc().compositeDirectBuffer( 2 ).addComponents( true, pre, msg.retain() ) );
                } else
                {
                    ByteBuf buf = ctx.alloc().directBuffer( lengthLen + 1 + oldBodyLen );
                    DefinedPacket.writeVarInt( oldBodyLen + 1, buf );
                    out.add( buf.writeByte( 0 ).writeBytes( msg ) ); // 0 indicates uncompressed
                }
            } else
            {
                ByteBuf buf = ctx.alloc().directBuffer( COMPRESS_BUFFER_SIZE + MAX_SUPPORTED_VARINT_LENGTH_LEN + varintSize( oldBodyLen ) );
                buf.writerIndex( MAX_SUPPORTED_VARINT_LENGTH_LEN ); // Reserve space for packet length varint
                DefinedPacket.writeVarInt( oldBodyLen, buf ); // write uncompressed length
                process( msg, buf ); // compress data to buf

                // write varint length of compressed directly infront of compressed data
                // leaves potential unused bytes at buffer start
                int writerIndex = buf.writerIndex();
                int compressedLen = writerIndex - MAX_SUPPORTED_VARINT_LENGTH_LEN;
                byte lengthLen = varintSize( compressedLen );
                int lengthStart = MAX_SUPPORTED_VARINT_LENGTH_LEN - lengthLen;
                DefinedPacket.setVarInt( compressedLen, buf, lengthStart, lengthLen );

                buf.readerIndex( lengthStart ); // set start of buffer to ignore potential unused bytes before length
                out.add( buf );
            }
        } else
        {
            byte lengthLen = varintSize( oldBodyLen );
            if ( ( flags & FLAG_TWO_BUFFERS ) != 0 )
            {
                ByteBuf lenBuf = ctx.alloc().directBuffer( lengthLen );
                DefinedPacket.writeVarInt( oldBodyLen, lenBuf );
                out.add( lenBuf );
                out.add( msg.retain() );
            } else if ( ( flags & FLAG_COMPOSE ) != 0 )
            {
                // create a virtual buffer to avoid copying of data
                ByteBuf pre = ctx.alloc().directBuffer( lengthLen );
                DefinedPacket.writeVarInt( oldBodyLen, pre );
                out.add( ctx.alloc().compositeDirectBuffer( 2 ).addComponents( true, pre, msg.retain() ) );
            } else
            {
                ByteBuf buf = ctx.alloc().directBuffer( lengthLen + oldBodyLen );
                DefinedPacket.writeVarInt( oldBodyLen, buf );
                out.add( buf.writeBytes( msg ) ); // 0 indicates uncompressed
            }
        }
    }

    private void process(ByteBuf in, ByteBuf out)
    {
        int buffersInIdx = 0;
        ByteBuffer[] buffersIn = in.nioBuffers();

        int buffersOutIdx = 0;
        ByteBuffer[] buffersOut = null;

        zlib.setInput( buffersIn[buffersInIdx] );

        while ( !zlib.finished() )
        {
            if ( buffersInIdx == buffersIn.length - 1 )
            {
                zlib.finish();
            }

            if ( out.writableBytes() < COMPRESS_BUFFER_SIZE )
            {
                out.ensureWritable( COMPRESS_BUFFER_SIZE );
                buffersOut = null;
            }

            if ( buffersOut == null )
            {
                buffersOutIdx = 0;
                buffersOut = out.nioBuffers( out.writerIndex(), out.writableBytes() );
            }

            int totalIn = zlib.getTotalIn();
            ByteBuffer bufferOut = buffersOut[buffersOutIdx];
            int written = zlib.deflate( bufferOut );
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
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        if ( zlib != null )
        {
            zlib.end();
            zlib = null;
        }
    }

    public void setCompose(boolean compose)
    {
        if ( compose )
        {
            flags |= FLAG_COMPOSE;
        } else
        {
            flags &= ~FLAG_COMPOSE;
        }
    }

    public boolean isCompress()
    {
        return ( flags & FLAG_COMPRESS ) != 0;
    }

    public void setCompress(boolean compress)
    {
        if ( compress )
        {
            if ( zlib == null )
            {
                zlib = new Deflater();
            }
            flags |= FLAG_COMPRESS;
        } else
        {
            flags &= ~FLAG_COMPRESS;
            if ( zlib != null )
            {
                zlib.end();
                zlib = null;
            }
        }
    }

    public void setTwoBuffers(boolean twoBuffers)
    {
        if ( twoBuffers )
        {
            flags |= FLAG_TWO_BUFFERS;
        } else
        {
            flags &= ~FLAG_TWO_BUFFERS;
        }
    }

    private static byte varintSize(int value)
    {
        if ( ( value & 0xFFFFFF80 ) == 0 )
        {
            return 1;
        }
        if ( ( value & 0xFFFFC000 ) == 0 )
        {
            return 2;
        }
        if ( ( value & 0xFFE00000 ) == 0 )
        {
            return 3;
        }
        if ( ( value & 0xF0000000 ) == 0 )
        {
            return 4;
        }
        throw new IllegalArgumentException( "Packet length " + value + " longer than supported (max. 268435455 for 4 byte varint)" );
    }
}
