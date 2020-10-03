package net.md_5.bungee.compress;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.protocol.DefinedPacket;
import ru.leymooo.botfilter.utils.FastBadPacketException;

public class PacketDecompressor extends MessageToMessageDecoder<ByteBuf>
{
    //BotFilter start - fix bungeecord crasher
    private static final int MAXIMUM_UNCOMPRESSED_SIZE = 4 * 1024 * 1024; // 4MiB
    private int threshold = -1;

    public void setThreshold(int threshold)
    {
        this.threshold = threshold;
    }
    //BotFilter end

    private BungeeZlib zlib;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        zlib = CompressFactory.zlib.newInstance();
        zlib.init( false, 0 );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        zlib.free();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        int size = DefinedPacket.readVarInt( in );
        if ( size == 0 )
        {
            out.add( in.slice().retain() );
            in.skipBytes( in.readableBytes() );
        } else
        {
            if ( threshold != -1 && size < threshold )
            {
                throw new FastBadPacketException( "Uncompressed size " + size + " is less than threshold " + threshold );
            }

            if ( size > MAXIMUM_UNCOMPRESSED_SIZE )
            {
                throw new FastBadPacketException( "Uncompressed size " + size + " exceeds threshold of " + MAXIMUM_UNCOMPRESSED_SIZE );
            }

            ByteBuf decompressed = ctx.alloc().directBuffer();

            try
            {
                zlib.process( in, decompressed );
                Preconditions.checkState( decompressed.readableBytes() == size, "Decompressed packet size mismatch" );

                out.add( decompressed );
                decompressed = null;
            } finally
            {
                if ( decompressed != null )
                {
                    decompressed.release();
                }
            }
        }
    }
}
