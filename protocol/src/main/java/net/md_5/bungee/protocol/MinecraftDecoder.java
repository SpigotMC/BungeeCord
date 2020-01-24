package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.net.InetSocketAddress;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.protocol.packet.Handshake;

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
            //BotFilter start
            if ( in.readableBytes() == 0 )
            {
                return;
            }
            //BotFilter end
            int packetId = DefinedPacket.readVarInt( in );
            DefinedPacket packet = prot.createPacket( packetId, protocolVersion );
            if ( packet != null )
            {
                if ( packet instanceof Handshake )
                {
                    try
                    {
                        packet.read( in, prot.getDirection(), protocolVersion );
                    } catch ( IndexOutOfBoundsException e )
                    {
                        ctx.close();
                        System.out.println( "[" + ( (InetSocketAddress) ctx.channel().remoteAddress() ).getAddress().getHostAddress() + "] sent wrong Handshake packet. Junk??)" );
                        return;
                    }
                } else
                {
                    packet.read( in, prot.getDirection(), protocolVersion );
                }
                if ( in.isReadable() )
                {
                    in.skipBytes( in.readableBytes() ); //BotFilter
                    throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() );
                }
            } else
            {
                in.skipBytes( in.readableBytes() );
            }

            //System.out.println( "ID: " + packetId + ( packet == null ? " (null)" : " ("+packet+")" ) );
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
