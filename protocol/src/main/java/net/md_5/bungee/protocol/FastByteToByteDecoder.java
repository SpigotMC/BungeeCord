package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;

/**
 * A decoder that decodes a received ByteBuf into another ByteBuf.
 */
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
            // not a ByteBuf, pass it along unmodified
            ctx.fireChannelRead( msg );
        }
    }

    /**
     * Decodes the given ByteBuf into another ByteBuf or modifies and returns the given ByteBuf.
     * Note: if the input ByteBuf is modified and returned, it needs to be retained.
     *
     * @param ctx the ChannelHandlerContext
     * @param in the ByteBuf to decode
     * @return the decoded ByteBuf or null if it should be discarded
     * @throws Exception decoding exception
     */
    protected abstract ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception;
}
