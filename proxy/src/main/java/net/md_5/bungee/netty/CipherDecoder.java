package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.ByteToMessageDecoder;
import javax.crypto.Cipher;

public class CipherDecoder extends ByteToMessageDecoder
{

    private final CipherBase cipher;

    public CipherDecoder(Cipher cipher)
    {
        this.cipher = new CipherBase( cipher );
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, MessageList<Object> out) throws Exception
    {
        out.add( cipher.cipher( ctx, in ) );
    }
}
