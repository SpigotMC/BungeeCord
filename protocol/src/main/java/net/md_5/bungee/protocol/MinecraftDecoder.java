package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
public class MinecraftDecoder extends ByteToMessageDecoder
{

    @Setter
    private Protocol protocol;
    private boolean server;
    @Setter
    private int protocolVersion;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        ByteBuf copy = in.copy(); // TODO

        int packetId;
        try {
            packetId = DefinedPacket.readVarInt( in );
        }
        catch(Exception e) {
            throw new DetailedBadPacketException("Exception decoding packet ID", e, null, null, protocol, prot.getDirection());
        }

        DefinedPacket packet = null;
        if ( prot.hasPacket( packetId ) )
        {
            packet = prot.createPacket( packetId );

            try {
                packet.read( in, prot.getDirection(), protocolVersion );
            }
            catch(Exception e) {
                throw new DetailedBadPacketException("Exception decoding packet", e, packetId, packet, protocol, prot.getDirection());
            }

            if ( in.readableBytes() != 0 )
            {
                throw new DetailedBadPacketException(in.readableBytes() + " bytes remain after decoding packet", packetId, packet, protocol, prot.getDirection());
            }
        } else
        {
            in.skipBytes( in.readableBytes() );
        }

        out.add( new PacketWrapper( packet, copy ) );
    }
}
