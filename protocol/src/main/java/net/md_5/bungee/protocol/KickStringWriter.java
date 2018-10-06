package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class KickStringWriter extends MessageToByteEncoder<String>
{

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception
    {
        out.writeByte( 0xFF );
        out.writeShort( msg.length() );
        for ( char c : msg.toCharArray() )
        {
            out.writeChar( c );
        }
    }
}
