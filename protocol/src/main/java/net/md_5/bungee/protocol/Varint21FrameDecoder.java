package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import ru.leymooo.botfilter.utils.FastCorruptedFrameException;

public class Varint21FrameDecoder extends ByteToMessageDecoder
{
    //BotFilter start
    private boolean fromBackend;

    public void setFromBackend(boolean fromBackend)
    {
        this.fromBackend = fromBackend;
    }
    //BotFilter end

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        //BotFilter start - rewrite Varint21Decoder
        if ( !ctx.channel().isActive() )
        {
            super.setSingleDecode( true );
            return;
        }

        int origReaderIndex = in.readerIndex();

        int i = 3;
        while ( i-- > 0 )
        {
            if ( !in.isReadable() )
            {
                in.readerIndex( origReaderIndex );
                return;
            }

            byte read = in.readByte();
            if ( read >= 0 )
            {
                // Make sure reader index of length buffer is returned to the beginning
                in.readerIndex( origReaderIndex );
                int packetLength = DefinedPacket.readVarInt( in );

                if ( packetLength <= 0 && !fromBackend )
                {
                    super.setSingleDecode( true );
                    throw new FastCorruptedFrameException( "Empty Packet!" );
                }

                if ( in.readableBytes() < packetLength )
                {
                    in.readerIndex( origReaderIndex );
                    return;
                }
                out.add( in.readRetainedSlice( packetLength ) );
                return;
            }
        }

        super.setSingleDecode( true );
        throw new FastCorruptedFrameException( "length wider than 21-bit" );
    }
    //BotFilter end
}
