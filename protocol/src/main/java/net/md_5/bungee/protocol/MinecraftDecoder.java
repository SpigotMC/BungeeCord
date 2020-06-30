package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.Setter;
import net.md_5.bungee.util.ChannelUtil;

public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf>
{

    @Setter
    private Protocol protocol;
    private final boolean server;
    @Setter
    private int protocolVersion;
    @Setter
    private boolean forge;

    public MinecraftDecoder(Protocol protocol, boolean server, int protocolVersion)
    {
        this.protocol = protocol;
        this.server = server;
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;

        int originalReaderIndex = in.readerIndex();
        int originalReadableBytes = in.readableBytes();
        ByteBuf slice = null; // Can't slice this one due to EntityMap :(

        try
        {
            int packetId = DefinedPacket.readVarInt( in );

            if ( server && !forge && ( packetId < 0 || packetId > Protocol.MAX_PACKET_ID ) )
            {
                ChannelUtil.shutdownChannel( ctx.channel(), null );
                return;
            }
            slice = in.copy( originalReaderIndex, originalReadableBytes );

            DefinedPacket packet = prot.createPacket( packetId, protocolVersion );
            if ( packet != null )
            {
                try
                {
                    packet.read( in, prot.getDirection(), protocolVersion );
                } catch ( Throwable t )
                {
                    if ( server )
                    {
                        ChannelUtil.shutdownChannel( ctx.channel(), t );
                        return;
                    }
                    throw t;
                }

                if ( in.isReadable() )
                {
                    if ( server )
                    {
                        ChannelUtil.shutdownChannel( ctx.channel(), new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() ) );
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
