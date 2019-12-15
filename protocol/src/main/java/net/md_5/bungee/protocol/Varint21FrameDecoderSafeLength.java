package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.util.List;
import lombok.RequiredArgsConstructor;

public class Varint21FrameDecoderSafeLength extends ByteToMessageDecoder
{
    // packetid + protocol version varint + server address(length varint + data) + server port + varint enum
    private static final int MIN_LENGTH_FIRST_PACKET = 1 + 1 + ( 1 + 1 ) + 2 + 1;
    private static final int MAX_LENGTH_FIRST_PACKET = 1 + 5 + ( 3 + 255 * 4 ) + 2 + 1;
    // packetid + username(string length varint + string data)
    private static final int MIN_LENGTH_SECOND_PACKET = 1 + 1 + 1;
    private static final int MAX_LENGTH_SECOND_PACKET = 1 + ( 1 + 16 * 4 );
    private boolean first = true;
    private boolean second = false;
    private boolean stop = false;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if ( stop )
        {
            return;
        }
        in.markReaderIndex();

        final byte[] buf = new byte[ 3 ];
        for ( int i = 0; i < buf.length; i++ )
        {
            if ( !in.isReadable() )
            {
                in.resetReaderIndex();
                return;
            }

            if ( ( buf[ i ] = in.readByte() ) >= 0 )
            {
                int length = DefinedPacket.readVarIntLengthSpecial( buf );
                if ( length <= 0 )
                {
                    final InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                    super.setSingleDecode( true );
                    ctx.pipeline().addFirst( InboundDiscardHandler.DISCARD_FIRST, InboundDiscardHandler.INSTANCE )
                            .addAfter( ctx.name(), InboundDiscardHandler.DISCARD, InboundDiscardHandler.INSTANCE );
                    ctx.close().addListener( new Varint21FrameDecoder.InvalidPacketLengthLogger( address, length ) );
                    stop = true;
                    return;
                }
                if ( first )
                {
                    if ( length < MIN_LENGTH_FIRST_PACKET || length > MAX_LENGTH_FIRST_PACKET )
                    {
                        final InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                        super.setSingleDecode( true );
                        ctx.pipeline().addFirst( InboundDiscardHandler.DISCARD_FIRST, InboundDiscardHandler.INSTANCE )
                                .addAfter( ctx.name(), InboundDiscardHandler.DISCARD, InboundDiscardHandler.INSTANCE );
                        ctx.close().addListener( new WrongFirstPacketLength( address, length ) );
                        stop = true;
                        return;
                    }
                } else if ( second )
                {
                    second = false;
                    if ( length != 1 && ( length < MIN_LENGTH_SECOND_PACKET || length > MAX_LENGTH_SECOND_PACKET ) )
                    {
                        final InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                        super.setSingleDecode( true );
                        ctx.pipeline().addFirst( InboundDiscardHandler.DISCARD_FIRST, InboundDiscardHandler.INSTANCE )
                                .addAfter( ctx.name(), InboundDiscardHandler.DISCARD, InboundDiscardHandler.INSTANCE );
                        ctx.close().addListener( new WrongSecondPacketLength( address, length ) );
                        stop = true;
                        return;
                    }
                }

                if ( in.readableBytes() < length )
                {
                    in.resetReaderIndex();
                    return;
                } else
                {
                    if ( first )
                    {
                        first = false;
                        second = true;
                    }
                    if ( in.hasMemoryAddress() )
                    {
                        out.add( in.slice( in.readerIndex(), length ).retain() );
                        in.skipBytes( length );
                    } else
                    {
                        if ( !Varint21FrameDecoder.DIRECT_WARNING )
                        {
                            Varint21FrameDecoder.DIRECT_WARNING = true;
                            System.out.println( "Netty is not using direct IO buffers." );
                        }

                        // See https://github.com/SpigotMC/BungeeCord/issues/1717
                        ByteBuf dst = ctx.alloc().directBuffer( length );
                        in.readBytes( dst );
                        out.add( dst );
                    }
                    return;
                }
            }
        }
        final InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        super.setSingleDecode( true );
        ctx.pipeline().addFirst( InboundDiscardHandler.DISCARD_FIRST, InboundDiscardHandler.INSTANCE )
                .addAfter( ctx.name(), InboundDiscardHandler.DISCARD, InboundDiscardHandler.INSTANCE );
        ctx.close().addListener( new Varint21FrameDecoder.PacketLengthFieldTooLongLogger( address ) );
        stop = true;
    }

    @RequiredArgsConstructor
    private static class WrongFirstPacketLength implements GenericFutureListener<Future<? super Void>>
    {
        private static final String LENGTH_INFO = " out of bounds (" + MIN_LENGTH_FIRST_PACKET + ", " + MAX_LENGTH_FIRST_PACKET + "), disconnected";
        private final InetSocketAddress address;
        private final int length;

        @Override
        public void operationComplete(Future<? super Void> future) throws Exception
        {
            System.err.println( "[" + address + "] <-> Varint21FrameDecoder first packet length " + length + LENGTH_INFO );
        }
    }

    @RequiredArgsConstructor
    private static class WrongSecondPacketLength implements GenericFutureListener<Future<? super Void>>
    {
        private static final String LENGTH_INFO = " out of bounds (" + MIN_LENGTH_SECOND_PACKET + ", " + MAX_LENGTH_SECOND_PACKET + "), disconnected";
        private final InetSocketAddress address;
        private final int length;

        @Override
        public void operationComplete(Future<? super Void> future) throws Exception
        {
            System.err.println( "[" + address + "] <-> Varint21FrameDecoder second packet length " + length + LENGTH_INFO );
        }
    }
}
