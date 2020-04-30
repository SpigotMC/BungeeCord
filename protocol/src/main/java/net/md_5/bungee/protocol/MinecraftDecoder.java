package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;

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
        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        ByteBuf slice = in.copy(); // Can't slice this one due to EntityMap :(

        try
        {
            int packetId = DefinedPacket.readVarInt( in, ProtocolConstants.PACKET_ID_MAXBYTES );

            if ( packetId < ProtocolConstants.PACKET_ID_MINIMUM || packetId > ProtocolConstants.PACKET_ID_MAXIMUM )
            {
                throw new BadPacketException( new StringBuilder( 18 + 5 + 10 + 5 + 11 + 9 ).append( "Invalid packet id " ).append( packetId ).append( " Protocol " ).append( protocol ).append( " Direction " ).append( prot.getDirection() ).toString() );
            }

            DefinedPacket<?> packet = prot.createPacket( packetId, protocolVersion );
            if ( packet != null )
            {
                packet.read( in, prot.getDirection(), protocolVersion );

                if ( in.isReadable() )
                {
                    throw new BadPacketException( new StringBuilder( 35 + 6 + 31 + 1 + 22 + 1 + 5 + 10 + 5 + 11 + 9 ).append( "Did not read all bytes from packet " ).append( packet.getClass() ).append( ' ' ).append( packetId ).append( " Protocol " ).append( protocol ).append( " Direction " ).append( prot.getDirection() ).toString() );
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
}
