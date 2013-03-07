package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.protocol.netty.PacketReader;

@AllArgsConstructor
public class PacketDecoder extends ReplayingDecoder<ByteBuf>
{

    @Getter
    @Setter
    private int protocol;

    @Override
    protected ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception
    {
        PacketReader.readPacket( in, protocol );
        return in.copy();
    }
}
