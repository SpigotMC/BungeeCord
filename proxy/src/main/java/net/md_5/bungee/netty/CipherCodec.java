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
    private ByteBuf heapOut;

    public CipherCodec(Cipher encrypt, Cipher decrypt)
    {
        this.encrypt = encrypt;
        this.decrypt = decrypt;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher( ctx, in, out, encrypt );
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        cipher( ctx, in, out, decrypt );
    }

    @Override
    public void freeInboundBuffer(ChannelHandlerContext ctx) throws Exception
    {
        super.freeInboundBuffer( ctx );
        if ( heapOut != null )
        {
            heapOut.release();
            heapOut = null;
        }
    }

    @Override
    public void freeOutboundBuffer(ChannelHandlerContext ctx) throws Exception
    {
        super.freeOutboundBuffer( ctx );
        if ( heapOut != null )
        {
            heapOut.release();
            heapOut = null;
        }
    }

    private void cipher(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out, Cipher cipher) throws Exception
    {
        synchronized ( this )
        {
            if ( heapOut == null )
            {
                heapOut = ctx.alloc().heapBuffer();
            }

            int available = in.readableBytes();
            int outputSize = cipher.getOutputSize( available );
            if ( heapOut.capacity() < outputSize )
            {
                heapOut.capacity( outputSize );
            }
            int processed = cipher.update( in.array(), in.arrayOffset() + in.readerIndex(), available, heapOut.array(), heapOut.arrayOffset() + heapOut.writerIndex() );
            in.readerIndex( in.readerIndex() + processed );
            heapOut.writerIndex( heapOut.writerIndex() + processed );

            out.writeBytes( heapOut );
            heapOut.discardSomeReadBytes();
        }
    }
}
