package net.md_5.bungee.compress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;
import lombok.Setter;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.protocol.DefinedPacket;

public class PacketCompressor extends MessageToByteEncoder<ByteBuf>
{

    private BungeeZlib zlib;
    @Setter
    private int threshold = 256;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        zlib = CompressFactory.zlib.newInstance();
        zlib.init( true, Deflater.DEFAULT_COMPRESSION );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        zlib.free();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception
    {
        int origSize = msg.readableBytes();
        if ( origSize < threshold )
        {
            DefinedPacket.writeVarInt( 0, out );
            out.writeBytes( msg );
        } else
        {
            DefinedPacket.writeVarInt( origSize, out );

            zlib.process( msg, out );
        }
    }
}
