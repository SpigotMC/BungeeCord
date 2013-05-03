package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Class to expose an
 * {@link #cipher(io.netty.buffer.ByteBuf, io.netty.buffer.ByteBuf)} method to
 * aid in the efficient passing of ByteBuffers through a cipher.
 */
@RequiredArgsConstructor
public class CipherBase
{

    @NonNull
    private final Cipher cipher;
    private ThreadLocal<byte[]> heapInLocal = new EmptyByteThreadLocal();
    private ThreadLocal<byte[]> heapOutLocal = new EmptyByteThreadLocal();

    private static class EmptyByteThreadLocal extends ThreadLocal<byte[]>
    {

        @Override
        protected byte[] initialValue()
        {
            return new byte[ 0 ];
        }
    }

    protected void cipher(ByteBuf in, ByteBuf out) throws ShortBufferException
    {
        byte[] heapIn = heapInLocal.get();
        int readableBytes = in.readableBytes();
        if ( heapIn.length < readableBytes )
        {
            heapIn = new byte[ readableBytes ];
        }
        in.readBytes( heapIn, 0, readableBytes );

        byte[] heapOut = heapOutLocal.get();
        int outputSize = cipher.getOutputSize( readableBytes );
        if ( heapOut.length < outputSize )
        {
            heapOut = new byte[ outputSize ];
        }
        out.writeBytes( heapOut, 0, cipher.update( heapIn, 0, readableBytes, heapOut ) );
    }
}
