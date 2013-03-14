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
    private ByteBuf heapIn;
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
        free();
    }

    @Override
    public void freeOutboundBuffer(ChannelHandlerContext ctx) throws Exception
    {
        super.freeOutboundBuffer( ctx );
        free();
    }

    private void free()
    {
        if ( heapIn != null )
        {
            heapIn.release();
            heapIn = null;
        }
        if ( heapOut != null )
        {
            heapOut.release();
            heapOut = null;
        }
    }

    private void cipher(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out, Cipher cipher) throws Exception
    {
        try
        {
            // Allocate input buffer
            if ( heapIn == null )
            {
                heapIn = ctx.alloc().heapBuffer();
            }

            // Allocate correct amount of space
            int readableBytes = in.readableBytes();
            heapIn.capacity( heapIn.writerIndex() + readableBytes );
            // Read into buffer
            in.readBytes( heapIn );

            // Allocate output buffer
            if ( heapOut == null )
            {
                heapOut = ctx.alloc().heapBuffer();
            }

            // Get output size
            int outputSize = cipher.getOutputSize( readableBytes );
            // Check we have enough space
            if ( heapOut.writableBytes() < outputSize )
            {
                heapOut.capacity( heapOut.writerIndex() + outputSize );
            }

            // Do the processing
            int processed = cipher.update( heapIn.array(), heapIn.arrayOffset() + heapIn.readerIndex(), readableBytes, heapOut.array(), heapOut.arrayOffset() + heapOut.writerIndex() );

            // Tell the out buffer we read some from it
            heapOut.writerIndex( heapOut.writerIndex() + processed );

            out.writeBytes( heapOut );
            heapIn.clear();
            heapOut.clear();
        } catch ( Throwable ex )
        {
            // TODO: Remove this once we are stable
            ex.printStackTrace();
        }
    }
}
