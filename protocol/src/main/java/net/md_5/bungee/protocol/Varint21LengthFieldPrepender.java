package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import lombok.Setter;

/**
 * Prepend length of the message as a Varint21 by writing length and data to a
 * new buffer
 */
public class Varint21LengthFieldPrepender extends MessageToMessageEncoder<ByteBuf>
{

    @Setter
    private boolean compose = true;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> list) throws Exception
    {
        int bodyLen = msg.readableBytes();
        int headerLen = varintSize( bodyLen );
        if ( compose )
        {
            ByteBuf buf = ctx.alloc().directBuffer( headerLen );
            DefinedPacket.writeVarInt( bodyLen, buf );
            list.add( ctx.alloc().compositeDirectBuffer( 2 ).addComponents( true, buf, msg.retain() ) );
        } else
        {
            ByteBuf buf = ctx.alloc().directBuffer( headerLen + bodyLen );
            DefinedPacket.writeVarInt( bodyLen, buf );
            buf.writeBytes( msg );
            list.add( buf );
        }
    }

    static int varintSize(int paramInt)
    {
        if ( ( paramInt & 0xFFFFFF80 ) == 0 )
        {
            return 1;
        }
        if ( ( paramInt & 0xFFFFC000 ) == 0 )
        {
            return 2;
        }
        if ( ( paramInt & 0xFFE00000 ) == 0 )
        {
            return 3;
        }
        if ( ( paramInt & 0xF0000000 ) == 0 )
        {
            return 4;
        }
        return 5;
    }
}
