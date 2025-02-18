package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.ByteProcessor;
import java.util.List;

public class Varint21FrameDecoder extends ByteToMessageDecoder
{
    private static final ByteProcessor CONTINUE_BIT_PROCESSOR = value -> value < 0;
    private static boolean DIRECT_WARNING;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        // If we decode an invalid packet and an exception is thrown (thus triggering a close of the connection),
        // the Netty ByteToMessageDecoder will continue to frame more packets and potentially call fireChannelRead()
        // on them, likely with more invalid packets. Therefore, check if the connection is no longer active and if so
        // silently discard the packet.
        if ( !ctx.channel().isActive() )
        {
            in.skipBytes( in.readableBytes() );
            return;
        }

        int maxVarIntBytes = Math.min( in.readableBytes(), 3 );
        int endIdx = in.forEachByte( in.readerIndex(), maxVarIntBytes, CONTINUE_BIT_PROCESSOR );
        if ( endIdx == -1 )
        {
            if ( in.readableBytes() < 3 ) // Not enough bytes for the packet length yet
            {
                return;
            }
            throw new CorruptedFrameException( "length wider than 21-bit" );
        }

        int varIntIndex = ( endIdx - in.readerIndex() );
        int varIntLen = varIntIndex + 1;

        int length = getVarInt( in, varIntIndex );
        if ( length == 0 )
        {
            throw new CorruptedFrameException( "Empty Packet!" );
        }

        if ( in.readableBytes() - varIntLen >= length )
        {
            in.skipBytes( varIntLen );
            if ( in.hasMemoryAddress() )
            {
                out.add( in.readRetainedSlice( length ) );
            } else
            {
                if ( !DIRECT_WARNING )
                {
                    DIRECT_WARNING = true;
                    System.out.println( "Netty is not using direct IO buffers." );
                }

                // See https://github.com/SpigotMC/BungeeCord/issues/1717
                ByteBuf dst = ctx.alloc().directBuffer( length );
                in.readBytes( dst );
                out.add( dst );
            }
        }
    }

    private static int getVarInt(ByteBuf byteBuf, int varIntIndex)
    {
        int readerIndex = byteBuf.readerIndex();
        if ( varIntIndex == 0 )
        {
            return byteBuf.getByte( readerIndex );
        } else if ( varIntIndex == 1 )
        {
            return byteBuf.getByte( readerIndex ) & 0x7F | byteBuf.getByte( readerIndex + 1 ) << 7;
        } else
        {
            return byteBuf.getByte( readerIndex ) & 0x7F | ( byteBuf.getByte( readerIndex + 1 ) & 0x7F ) << 7 | byteBuf.getByte( readerIndex + 2 ) << 14;
        }
    }
}
