package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteCodec;
import javax.crypto.Cipher;

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

    private void cipher(Cipher cipher, ByteBuf in, ByteBuf out) throws Exception
    {
        try
        {
            int available = in.readableBytes();
            int outputSize = cipher.getOutputSize( available );
            int writerIndex = out.writerIndex();
            if ( out.capacity() + writerIndex < outputSize )
            {
                out.capacity( outputSize + writerIndex );
            }
            int processed = cipher.update( in.nioBuffer(), out.nioBuffer( out.writerIndex(), outputSize ) );
            in.readerIndex( in.readerIndex() + processed );
            out.writerIndex( writerIndex + processed );
        } catch ( Exception ex )
        {
            ex.printStackTrace();
            throw ex;
        }
    }
}
