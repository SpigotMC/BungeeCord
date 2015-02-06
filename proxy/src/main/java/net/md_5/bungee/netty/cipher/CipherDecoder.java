package net.md_5.bungee.netty.cipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import java.util.List;

@RequiredArgsConstructor
public class CipherDecoder extends MessageToMessageDecoder<ByteBuf>
{

    private final BungeeCipher cipher;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception
    {
        out.add( cipher.cipher( ctx, msg ) );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        cipher.free();
    }
}
