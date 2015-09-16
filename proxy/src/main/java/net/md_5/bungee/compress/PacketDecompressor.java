package net.md_5.bungee.compress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.protocol.DefinedPacket;

public class PacketDecompressor extends ByteToMessageDecoder
{

    private final BungeeZlib zlib = CompressFactory.zlib.newInstance();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        zlib.init( false, 0 );
    }

    @Override
    public void handlerRemoved0(ChannelHandlerContext ctx) throws Exception
    {
        zlib.free();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if ( in.readableBytes() == 0 )
        {
            return;
        }

        int size = DefinedPacket.readVarInt( in );
        if ( size == 0 )
        {
            out.add( in.copy() );
            in.readerIndex( in.writerIndex() );
        } else
        {
            ByteBuf decompressed = ctx.alloc().directBuffer();
            zlib.process( in, decompressed );

            out.add( decompressed );
        }
    }
}
