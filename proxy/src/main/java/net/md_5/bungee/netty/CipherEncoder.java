package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteEncoder;
import javax.crypto.Cipher;

public class CipherEncoder extends ByteToByteEncoder
{

    private final CipherBase cipher;

    public CipherEncoder(Cipher cipher)
    {
        this.cipher = new CipherBase( cipher );
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher.cipher( in, out );
    }
}
