package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.leymooo.botfilter.discard.ChannelShutdownTracker;
import ru.leymooo.botfilter.discard.ErrorStream;

@RequiredArgsConstructor
public class Varint21FrameDecoder extends ByteToMessageDecoder
{
    private final ChannelShutdownTracker shutdownTracker;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        val tracker = this.shutdownTracker;
        if ( tracker.isShuttedDown() )
        {
            return;
        }
        int origReaderIndex = in.readerIndex();

        int i = 3;
        while ( i-- > 0 )
        {
            if ( !in.isReadable() )
            {
                in.readerIndex( origReaderIndex );
                return;
            }

            byte read = in.readByte();
            if ( read >= 0 )
            {
                // Make sure reader index of length buffer is returned to the beginning
                in.readerIndex( origReaderIndex );
                int packetLength = DefinedPacket.readVarInt( in );

                if ( packetLength <= 0 )
                {
                    super.setSingleDecode( true );
                    tracker.shutdown( ctx ).addListener( (ChannelFutureListener) future ->
                    {
                        ErrorStream.error( "[" + future.channel().remoteAddress() + "] <-> Varint21FrameDecoder received invalid packet length " + packetLength + ", disconnected" );
                    } );
                    return;
                }

                if ( in.readableBytes() < packetLength )
                {
                    in.readerIndex( origReaderIndex );
                    return;
                }
                out.add( in.readRetainedSlice( packetLength ) );
                return;
            }
        }

        super.setSingleDecode( true );
        tracker.shutdown( ctx ).addListener( (ChannelFutureListener) future ->
        {
            ErrorStream.error( "[" + future.channel().remoteAddress() + "] <-> Varint21FrameDecoder packet length field too long, disconnected" );
        } );
        //throw new CorruptedFrameException( "length wider than 21-bit" );
    }
}
