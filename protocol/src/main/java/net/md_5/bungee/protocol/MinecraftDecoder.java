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
    private boolean server;
    @Setter
    private int protocolVersion;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        ByteBuf copy = in.copy(); // TODO

        try
        {
            int packetId = DefinedPacket.readVarInt( in );

            DefinedPacket packet = null;
            if ( prot.hasPacket( packetId ) )
            {
                packet = prot.createPacket( packetId );
                packet.read( in, prot.getDirection(), protocolVersion );
                if ( in.readableBytes() != 0 )
                {
                    throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot );
                }
            } else
            {
                in.skipBytes( in.readableBytes() );
            }

            out.add( new PacketWrapper( packet, copy ) );
            copy = null;
        } finally
        {
            if ( copy != null )
            {
                copy.release();
            }
        }
    }
}
