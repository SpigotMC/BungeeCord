package net.md_5.bungee.compress;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.OverflowPacketException;

public class PacketDecompressor extends MessageToMessageDecoder<ByteBuf>
{

    private static final int MAX_DECOMPRESSED_LEN = 1 << 23;

    private BungeeZlib zlib;

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        setEnabled( false );
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if ( zlib == null )
        {
            out.add( in.retain() );
            return;
        }

        int size = DefinedPacket.readVarInt( in );
        if ( size == 0 )
        {
            out.add( in.retain() );
        } else
        {
            if ( size > MAX_DECOMPRESSED_LEN )
            {
                throw new OverflowPacketException( "Packet may not be larger than " + MAX_DECOMPRESSED_LEN + " bytes" );
            }

            // Do not use size as max capacity, as its possible that the entity rewriter increases the size afterwards
            // This would result in a kick (it happens rarely as the entity ids size must differ)
            ByteBuf decompressed = ctx.alloc().directBuffer( size, MAX_DECOMPRESSED_LEN );
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

    public void setEnabled(boolean enabled)
    {
        if ( enabled && this.zlib == null )
        {
            zlib = CompressFactory.zlib.newInstance();
            zlib.init( false, 0 );
        } else if ( !enabled && this.zlib != null )
        {
            zlib.free();
            zlib = null;
        }
    }

}
