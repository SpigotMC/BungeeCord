package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public abstract class ClearableByteToMessageDecoder extends ByteToMessageDecoder
{

    public static final Object CLEAR_PIPELINE = new Object();

    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        // clear internal buffer
        in.clear();
        super.decodeLast( ctx, in, out );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        // clear internal buffer, so there is no more stuff going to the pipeline after channel is closed
        internalBuffer().clear();
        super.channelInactive( ctx );
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        if ( evt == CLEAR_PIPELINE )
        {
            internalBuffer().clear();
        }
        super.userEventTriggered( ctx, evt );
    }

    @Override
    protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
}
