package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
public class MinecraftCodec extends MessageToMessageCodec<ByteBuf, DefinedPacket>
{

    @Setter
    private Protocol protocol;
    private boolean server;

    @Override
    protected void encode(ChannelHandlerContext ctx, DefinedPacket msg, List<Object> out) throws Exception
    {
        Protocol.ProtocolDirection prot = ( server ) ? protocol.TO_CLIENT : protocol.TO_SERVER;

        ByteBuf buf = ctx.alloc().buffer();
        DefinedPacket.writeVarInt( prot.getId( msg.getClass() ), buf );
        msg.write( buf );
        
        out.add( buf );
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception
    {
        Protocol.ProtocolDirection prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;

        int packetId = DefinedPacket.readVarInt( msg );

        ByteBuf copy = msg.copy();
        DefinedPacket packet = null;
        if ( prot.hasPacket( packetId ) )
        {
            packet = prot.createPacket( packetId );
            packet.read( msg );
        }

        out.add( new PacketWrapper( packet, copy ) );
    }
}
