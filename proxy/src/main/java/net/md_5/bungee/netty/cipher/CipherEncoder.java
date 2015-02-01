package net.md_5.bungee.netty.cipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.jni.cipher.BungeeCipher;

@RequiredArgsConstructor
public class CipherEncoder extends MessageToByteEncoder<ByteBuf>
{

    private final BungeeCipher cipher;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher.cipher( in, out );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        cipher.free();
    }
}
