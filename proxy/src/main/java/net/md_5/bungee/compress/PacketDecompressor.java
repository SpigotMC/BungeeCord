package net.md_5.bungee.compress;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.FastByteToByteDecoder;
import net.md_5.bungee.protocol.OverflowPacketException;

public class PacketDecompressor extends FastByteToByteDecoder
{

    private static final int MAX_DECOMPRESSED_LEN = 1 << 23;
    private final BungeeZlib zlib = CompressFactory.zlib.newInstance();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        zlib.init( false, 0 );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        zlib.free();
    }

    @Override
    protected ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception
    {
        int size = DefinedPacket.readVarInt( in );
        if ( size == 0 )
        {
            return in.retain();
        } else
        {
            if ( size > MAX_DECOMPRESSED_LEN )
            {
                throw new OverflowPacketException( "Packet may not be larger than " + MAX_DECOMPRESSED_LEN + " bytes" );
            }

            // Do not use size as max capacity, as its possible that the entity rewriter increases the size afterwards
            // This would result in a kick (it happens rarely as the entity ids size must differ)
            ByteBuf decompressed = ctx.alloc().directBuffer( size, MAX_DECOMPRESSED_LEN );
            boolean release = true;
            try
            {
                zlib.process( in, decompressed );
                Preconditions.checkState( decompressed.readableBytes() == size, "Decompressed packet size mismatch" );

                release = false;
                return decompressed;
            } finally
            {
                if ( release )
                {
                    decompressed.release();
                }
            }
        }
    }
}
