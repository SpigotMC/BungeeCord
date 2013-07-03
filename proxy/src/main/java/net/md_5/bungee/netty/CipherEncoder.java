package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.bouncycastle.crypto.BufferedBlockCipher;

public class CipherEncoder extends MessageToByteEncoder<ByteBuf>
{

    private final CipherBase cipher;

    public CipherEncoder(BufferedBlockCipher cipher)
    {
        this.cipher = new CipherBase( cipher );
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher.cipher( in, out );
    }
}
