package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.val;
import ru.leymooo.botfilter.discard.ChannelShutdownTracker;
import ru.leymooo.botfilter.discard.ErrorStream;

@AllArgsConstructor
public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf>
{

    @Setter
    private Protocol protocol;
    private final boolean server;
    @Setter
    private int protocolVersion;
    private ChannelShutdownTracker shutdownTracker;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        val tracker = this.shutdownTracker;
        if ( tracker.isShuttedDown() )
        {
            return;
        }


        int originalReaderIndex = in.readerIndex();
        int originalReadableBytes = in.readableBytes();
        int packetId = DefinedPacket.readVarInt( in );
        if ( packetId < 0 || packetId > Protocol.MAX_PACKET_ID )
        {
            tracker.shutdown( ctx ).addListener( (ChannelFutureListener) future ->
            {
                ErrorStream.error( "[" + ctx.channel().remoteAddress() + "] <-> MinecraftDecoder received invalid packet id " + packetId + ", disconnected" );
            } );
            return;
        }
        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        int protocolVersion = this.protocolVersion;
        DefinedPacket packet = prot.createPacket( packetId, protocolVersion );
        if ( packet != null )
        {
            packet.read( in, prot.getDirection(), protocolVersion );
            if ( in.isReadable() )
            {
                if ( server )
                {
                    tracker.shutdown( ctx ).addListener( (ChannelFutureListener) future ->
                    {
                        ErrorStream.error( "[" + ctx.channel().remoteAddress() + "] Longer than expected: Packet " + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() );
                    } );
                    return;
                }
                in.skipBytes( in.readableBytes() ); //BotFilter
                throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() );
            }
        } else
        {
            in.skipBytes( in.readableBytes() );
        }
        //System.out.println( "ID: " + packetId + ( packet == null ? " (null)" : " ("+packet+")" ) );
        ByteBuf slice = in.copy( originalReaderIndex, originalReadableBytes );
        out.add( new PacketWrapper( packet, slice ) );
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception
    {
        return msg instanceof ByteBuf;
    }
}
