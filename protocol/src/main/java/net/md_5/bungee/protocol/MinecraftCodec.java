package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MinecraftCodec extends MessageToMessageCodec<ByteBuf, DefinedPacket>
{

    private Protocol protocol;

    @Override
    protected void encode(ChannelHandlerContext ctx, DefinedPacket msg, List<Object> out) throws Exception
    {
        ByteBuf buf = ctx.alloc().buffer();
        DefinedPacket.writeVarInt( protocol.getId( msg.getClass() ), buf );
        msg.write( buf );
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception
    {
        int packetId = DefinedPacket.readVarInt( msg );

        ByteBuf copy = msg.copy();
        DefinedPacket packet = null;
        if ( protocol.hasPacket( packetId ) )
        {
            packet = protocol.createPacket( packetId );
            packet.read( msg );
        }

        out.add( new PacketWrapper( packet, copy ) );
    }
}
