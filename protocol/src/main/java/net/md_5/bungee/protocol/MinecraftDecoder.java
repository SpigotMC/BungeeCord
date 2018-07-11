package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf>
{

    @Setter
    private Protocol protocol;
    private final boolean server;
    @Setter
    private int protocolVersion;
    @Setter
    private boolean[] copiedBuffers;
    @Setter
    private boolean[] handledPackets;

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
        ByteBuf buf = null;

        try
        {
            int markIndex = in.readerIndex();
            int markReadable = in.readableBytes();
            int packetId = DefinedPacket.readVarInt( in );
            if ( copiedBuffers != null && packetId >= 0 && packetId < copiedBuffers.length && copiedBuffers[packetId] )
            {
                buf =  in.copy( markIndex, markReadable ); // Can't slice this one due to EntityMap varint :(
            } else
            {
                buf =  in.slice( markIndex, markReadable ).retain();
            }

            DefinedPacket packet = null;
            if ( handledPackets == null || ( packetId >= 0 && packetId < handledPackets.length && handledPackets[packetId] ) )
            {
                packet = prot.createPacket( packetId, protocolVersion );
                if ( packet != null )
                {
                    packet.read( in, prot.getDirection(), protocolVersion );

                    if ( in.isReadable() )
                    {
                        throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot );
                    }
                } else
                {
                    in.skipBytes( in.readableBytes() );
                }
            }

            out.add( new PacketWrapper( packet, buf ) );
            buf = null;
        } finally
        {
            if ( buf != null )
            {
                buf.release();
            }
        }
    }
}
