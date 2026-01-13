package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;

public abstract class FastByteToByteDecoder extends ChannelInboundHandlerAdapter
{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if ( msg instanceof ByteBuf )
        {
            ByteBuf out;
            try
            {
                out = decode( ctx, (ByteBuf) msg );
            } catch ( DecoderException e )
            {
                throw e;
            } catch ( Exception e )
            {
                throw new DecoderException( e );
            } finally 
            {
                ReferenceCountUtil.release( msg );   
            }
            if ( out != null )
            {
                ctx.fireChannelRead( out );
            }
        } else
        {
            ctx.fireChannelRead( msg );
        }
    }


    protected abstract ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception;
}
