package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteDecoder;
import javax.crypto.Cipher;

public class CipherDecoder extends ByteToByteDecoder
{

    private final CipherBase cipher;

    public CipherDecoder(Cipher cipher)
    {
        this.cipher = new CipherBase( cipher );
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher.cipher( in, out );
    }
}
