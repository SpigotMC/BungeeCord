package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf>
{

    @Setter
    private Protocol protocol;
    private final boolean server;
    @Setter
    private int protocolVersion;
    private boolean stop = false;

    public MinecraftDecoder(Protocol protocol, boolean server, int protocolVersion)
    {
        this.protocol = protocol;
        this.server = server;
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if ( stop )
        {
            return;
        }
        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;

        // When server is true, its client to server connection
        if ( server && in.readableBytes() < 1 )
        {
            final InetAddress address = ( (InetSocketAddress) ctx.channel().remoteAddress() ).getAddress();
            ctx.pipeline().addFirst( DiscardHandler.DISCARD_FIRST, DiscardHandler.INSTANCE )
                    .addAfter( ctx.name(), DiscardHandler.DISCARD, DiscardHandler.INSTANCE );
            ctx.close().addListener( new EmptyPacketLogger( address ) );
            stop = true;
            return;
        }

        ByteBuf slice = in.copy(); // Can't slice this one due to EntityMap :(

        try
        {
            int packetId = DefinedPacket.readVarIntPacketIdSpecial( in );

            if ( packetId < 0 || packetId > 0xFF )
            {
                ctx.pipeline().addFirst( DiscardHandler.DISCARD_FIRST, DiscardHandler.INSTANCE )
                        .addAfter( ctx.name(), DiscardHandler.DISCARD, DiscardHandler.INSTANCE );
                final InetAddress address = ( (InetSocketAddress) ctx.channel().remoteAddress() ).getAddress();
                ctx.close().addListener( new InvalidPacketIdLogger( address, packetId ) );
                stop = true;
                return;
            }

            DefinedPacket<?> packet = prot.createPacket( packetId, protocolVersion );
            if ( packet != null )
            {
                packet.read( in, prot.getDirection(), protocolVersion );

                if ( in.isReadable() )
                {
                    if ( server )
                    {
                        ctx.pipeline().addFirst( DiscardHandler.DISCARD_FIRST, DiscardHandler.INSTANCE )
                                .addAfter( ctx.name(), DiscardHandler.DISCARD, DiscardHandler.INSTANCE );
                        final InetAddress address = ( (InetSocketAddress) ctx.channel().remoteAddress() ).getAddress();
                        ctx.close().addListener( new IncompleteReadLogger( address, packet, packetId, prot ) );
                        stop = true;
                        return;
                    } else
                    {
                        throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() );
                    }
                }
            } else
            {
                in.skipBytes( in.readableBytes() );
            }

            out.add( new PacketWrapper<>( packet, slice ) );
            slice = null;
        } finally
        {
            if ( slice != null )
            {
                slice.release();
            }
        }
    }

    @RequiredArgsConstructor
    private static class EmptyPacketLogger implements GenericFutureListener<Future<? super Void>>
    {
        private final InetAddress address;

        @Override
        public void operationComplete(Future<? super Void> future) throws Exception
        {
            System.err.println( "[" + address.getHostAddress() + "] <-> MinecraftDecoder recieved empty packet, disconnected" );
        }
    }

    @RequiredArgsConstructor
    private static class InvalidPacketIdLogger implements GenericFutureListener<Future<? super Void>>
    {
        private final InetAddress address;
        private final int packetId;

        @Override
        public void operationComplete(Future<? super Void> future) throws Exception
        {
            System.err.println( "[" + address.getHostAddress() + "] <-> Varint21FrameDecoder recieved invalid packet id " + packetId + ", disconnected" );
        }
    }

    @RequiredArgsConstructor
    private class IncompleteReadLogger implements GenericFutureListener<Future<? super Void>>
    {
        private final InetAddress address;
        private final DefinedPacket<?> packet;
        private final int packetId;
        private final Protocol.DirectionData prot;

        @Override
        public void operationComplete(Future<? super Void> future) throws Exception
        {
            System.err.println( "[" + address.getHostAddress() + "] Longer than expected: Packet " + packet.getClass().getSimpleName() + ' ' + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() );
        }
    }
}
