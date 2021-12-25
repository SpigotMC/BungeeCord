package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;
import ru.leymooo.botfilter.utils.FastBadPacketException;

@AllArgsConstructor
public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf>
{

    @Setter
    private Protocol protocol;
    private final boolean server;
    @Setter
    private int protocolVersion;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        // See Varint21FrameDecoder for the general reasoning. We add this here as ByteToMessageDecoder#handlerRemoved()
        // will fire any cumulated data through the pipeline, so we want to try and stop it here.
        if ( !ctx.channel().isActive() )
        {
            return;
        }

        //BotFilter start
        if ( !server && in.readableBytes() == 0 ) //Fix empty packet from server
        {
            return;
        }
        int originalReaderIndex = in.readerIndex();
        int originalReadableBytes = in.readableBytes();
        int packetId = DefinedPacket.readVarInt( in );
        if ( packetId < 0 || packetId > Protocol.MAX_PACKET_ID )
        {
            throw new FastBadPacketException( "[" + ctx.channel().remoteAddress() + "] <-> MinecraftDecoder received invalid packet id " + packetId );
        }
        //BotFilter end
        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        int protocolVersion = this.protocolVersion;
        DefinedPacket packet = prot.createPacket( packetId, protocolVersion );
        if ( packet != null )
        {
            packet.read( in, prot.getDirection(), protocolVersion );
            if ( in.isReadable() )
            {
                in.skipBytes( in.readableBytes() ); //BotFilter
                throw new FastBadPacketException( "Packet " + protocol + ":" + prot.getDirection() + "/" + packetId + " (" + packet.getClass().getSimpleName() + ") larger than expected, extra bytes: " + in.readableBytes() );
            }
        } else
        {
            in.skipBytes( in.readableBytes() );
        }
        //System.out.println( "ID: " + packetId + ( packet == null ? " (null)" : " ("+packet+")" ) );
        ByteBuf copy = in.copy( originalReaderIndex, originalReadableBytes ); //BotFilter
        out.add( new PacketWrapper( packet, copy ) );
    }
}
