package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.protocol.netty.PacketReader;

/**
 * This class will attempt to read a packet from {@link PacketReader}, with the
 * specified {@link #protocol} before returning a new {@link ByteBuf} with the
 * copied contents of all bytes read in this frame.
 * <p/>
 * It is based on {@link ReplayingDecoder} so that packets will only be returned
 * when all needed data is present.
 */
@AllArgsConstructor
public class PacketDecoder extends ReplayingDecoder<Void>
{

    @Getter
    @Setter
    private int protocol;

    @Override
    protected byte[] decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception
    {
        int startIndex = in.readerIndex();
        PacketReader.readPacket( in, protocol );
        byte[] buf = new byte[ in.readerIndex() - startIndex ];
        in.readerIndex( startIndex );
        in.readBytes( buf, 0, buf.length );
        return buf;
    }
}
