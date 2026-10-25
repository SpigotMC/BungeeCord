package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf>
{

    public MinecraftDecoder(Protocol protocol, boolean server, int protocolVersion)
    {
        this( protocol, server, protocolVersion, shouldCopyBuffer( protocol, protocolVersion ) );
    }

    @Getter
    private Protocol protocol;
    private final boolean server;
    @Setter
    private int protocolVersion;
    private boolean copyBuffer;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        // See Varint21FrameDecoder for the general reasoning. We add this here as ByteToMessageDecoder#handlerRemoved()
        // will fire any cumulated data through the pipeline, so we want to try and stop it here.
        if ( !ctx.channel().isActive() )
        {
            return;
        }

        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        ByteBuf slice = ( copyBuffer ) ? in.copy() : in.retainedSlice();
        try
        {
            int packetId = DefinedPacket.readVarInt( in );

            DefinedPacket packet = prot.createPacket( packetId, protocolVersion );
            if ( packet != null )
            {
                packet.read( in, protocol, prot.getDirection(), protocolVersion );

                if ( in.isReadable() )
                {
                    throw new BadPacketException( "Packet " + protocol + ":" + prot.getDirection() + "/" + packetId + " (" + packet.getClass().getSimpleName() + ") larger than expected, extra bytes: " + in.readableBytes() );
                }
            } else
            {
                in.skipBytes( in.readableBytes() );
            }

            out.add( new PacketWrapper( packet, slice, protocol ) );
            slice = null;
        } finally
        {
            if ( slice != null )
            {
                slice.release();
            }
        }
    }

    public void setProtocol(Protocol protocol)
    {
        this.protocol = protocol;
        this.copyBuffer = shouldCopyBuffer( protocol, protocolVersion );
    }

    private static boolean shouldCopyBuffer(Protocol protocol, int protocolVersion)
    {
        // We only use the entity map in game state, we can avoid many buffer copies by checking this
        // EntityMap is removed for 1.20.2 and up
        return protocol == Protocol.GAME && protocolVersion < ProtocolConstants.MINECRAFT_1_20_2;
    }
}
