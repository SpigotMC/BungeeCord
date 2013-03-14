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
    private byte[] heapIn = new byte[ 0 ];
    private byte[] heapOut = new byte[ 0 ];

    public CipherCodec(Cipher encrypt, Cipher decrypt)
    {
        this.encrypt = encrypt;
        this.decrypt = decrypt;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher( in, out, encrypt );
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher( in, out, decrypt );
    }

    private void cipher(ByteBuf in, ByteBuf out, Cipher cipher) throws ShortBufferException
    {
        int readableBytes = in.readableBytes();
        if ( heapIn.length < readableBytes )
        {
            heapIn = new byte[ readableBytes ];
        }
        in.readBytes( heapIn, 0, readableBytes );

        int outputSize = cipher.getOutputSize( readableBytes );
        if ( heapOut.length < outputSize )
        {
            heapOut = new byte[ outputSize ];
        }
        out.writeBytes( heapOut, 0, cipher.update( heapIn, 0, readableBytes, heapOut ) );
    }
}
