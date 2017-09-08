package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.net.InetSocketAddress;
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
            int packetId = DefinedPacket.readVarInt( in );

            DefinedPacket packet = prot.createPacket( packetId, protocolVersion );
            if ( packet != null )
            {
                try
                {
                    packet.read( in, prot.getDirection(), protocolVersion );
                } catch ( Exception e )
                {
                    ctx.close();
                    System.out.println( "["+((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress()+"] Wrong packet: " + packet.getClass() + ", id: " + packetId + ", exeption: " + e);
                    return;
                }
                if ( in.isReadable() )
                {
                    throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot );
                }
            } else
            {
                in.skipBytes( in.readableBytes() );
            }

            out.add( new PacketWrapper( packet, slice ) );
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
