package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.util.List;
import lombok.RequiredArgsConstructor;

public class Varint21FrameDecoder extends ByteToMessageDecoder
{

    static boolean DIRECT_WARNING;

    private boolean stop;

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
                    ctx.close().addListener( new InvalidPacketLengthLogger( address, length ) );
                    stop = true;
                    return;
                }

                if ( in.readableBytes() < length )
                {
                    in.resetReaderIndex();
                    return;
                } else
                {
                    if ( in.hasMemoryAddress() )
                    {
                        out.add( in.slice( in.readerIndex(), length ).retain() );
                        in.skipBytes( length );
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
                    return;
                }
            }
        }

        final InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        super.setSingleDecode( true );
        ctx.pipeline().addFirst( InboundDiscardHandler.DISCARD_FIRST, InboundDiscardHandler.INSTANCE )
                .addAfter( ctx.name(), InboundDiscardHandler.DISCARD, InboundDiscardHandler.INSTANCE );
        ctx.close().addListener( new PacketLengthFieldTooLongLogger( address ) );
        stop = true;
    }

    @RequiredArgsConstructor
    static class InvalidPacketLengthLogger implements GenericFutureListener<Future<? super Void>>
    {
        private final InetSocketAddress address;
        private final int length;

        @Override
        public void operationComplete(Future<? super Void> future) throws Exception
        {
            System.err.println( "[" + address + "] <-> Varint21FrameDecoder recieved invalid packet length " + length + ", disconnected" );
        }
    }

    @RequiredArgsConstructor
    static class PacketLengthFieldTooLongLogger implements GenericFutureListener<Future<? super Void>>
    {
        private final InetSocketAddress address;

        @Override
        public void operationComplete(Future<? super Void> future) throws Exception
        {
            System.err.println( "[" + address + "] <-> Varint21FrameDecoder packet length field too long" );
        }
    }
}
