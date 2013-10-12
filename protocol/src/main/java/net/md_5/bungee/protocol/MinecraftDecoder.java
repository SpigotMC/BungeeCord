package net.md_5.bungee.protocol;

import com.google.common.base.Charsets;
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

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        Protocol.ProtocolDirection prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        ByteBuf copy = in.copy();

        int packetId = DefinedPacket.readVarInt( in );

        DefinedPacket packet = null;
        if ( prot.hasPacket( packetId ) )
        {
            packet = prot.createPacket( packetId );
            packet.read( in );
            if ( in.readableBytes() != 0 )
            {
                System.out.println( in.toString( Charsets.UTF_8 ) );
                throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot );
            }
        }

        out.add( new PacketWrapper( packet, copy ) );
    }
}
