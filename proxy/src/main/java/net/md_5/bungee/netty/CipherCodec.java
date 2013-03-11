package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteCodec;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

/**
 * This class is a complete solution for encrypting and decoding bytes in a
 * Netty stream. It takes two {@link Cipher} instances, used for encryption and
 * decryption respectively.
 */
public class CipherCodec extends ByteToByteCodec
{

    private Cipher encrypt;
    private Cipher decrypt;

    public CipherCodec(Cipher encrypt, Cipher decrypt)
    {
        this.encrypt = encrypt;
        this.decrypt = decrypt;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher( encrypt, in, out );
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher( decrypt, in, out );
    }

    private void cipher(Cipher cipher, ByteBuf in, ByteBuf out) throws ShortBufferException
    {
        int available = in.readableBytes();
        int outputSize = cipher.getOutputSize( available );
        if ( out.capacity() < outputSize )
        {
            out.capacity( outputSize );
        }
        int processed = cipher.update( in.nioBuffer(), out.nioBuffer( out.writerIndex(), outputSize ) );
        in.readerIndex( in.readerIndex() + processed );
        out.writerIndex( out.writerIndex() + processed );
    }
}
