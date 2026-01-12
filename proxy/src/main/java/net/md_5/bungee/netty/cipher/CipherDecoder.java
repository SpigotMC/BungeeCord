package net.md_5.bungee.netty.cipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.protocol.FastByteToByteDecoder;

@RequiredArgsConstructor
public class CipherDecoder extends FastByteToByteDecoder
{

    private final BungeeCipher cipher;

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        cipher.free();
    }

    @Override
    protected ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception
    {
        return cipher.cipher( ctx, in );
    }
}
