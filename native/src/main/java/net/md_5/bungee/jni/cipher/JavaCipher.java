package net.md_5.bungee.jni.cipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.FastThreadLocal;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

public class JavaCipher implements BungeeCipher
{

    private boolean type;
    private final Cipher cipher;
    private static final FastThreadLocal<ByteBuffer> bufferOutLocal = new EmptyByteThreadLocal();

    private static class EmptyByteThreadLocal extends FastThreadLocal<ByteBuffer>
    {

        @Override
        protected ByteBuffer initialValue()
        {
            return ByteBuffer.allocate( 0 );
        }
    }

    public JavaCipher()
    {
        try
        {
            this.cipher = Cipher.getInstance( "AES/CFB8/NoPadding" );
        } catch ( GeneralSecurityException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public void init(boolean forEncryption, SecretKey key) throws GeneralSecurityException
    {
        this.type = forEncryption;
        int mode = forEncryption ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
        cipher.init( mode, key, new IvParameterSpec( key.getEncoded() ) );
    }

    @Override
    public void cipher(ByteBuf in, ByteBuf out) throws ShortBufferException
    {
        System.out.println( this.type ? "enc" : "dec" );
        int readableBytes = in.readableBytes();

        ByteBuffer bufferOut = bufferOutLocal.get();
        int outputSize = cipher.getOutputSize( readableBytes );
        if ( bufferOut.capacity() < outputSize )
        {
            bufferOut = ByteBuffer.allocate( outputSize );
            bufferOutLocal.set( bufferOut );
        }
        bufferOut.clear();

        ByteBuffer[] buffersIn = in.nioBuffers();
        for ( ByteBuffer bufferIn : buffersIn )
        {
            cipher.update( bufferIn, bufferOut );
        }
        bufferOut.flip();

        out.writeBytes( bufferOut );
    }

    @Override
    public ByteBuf cipher(ChannelHandlerContext ctx, ByteBuf in) throws ShortBufferException
    {
        int readableBytes = in.readableBytes();
        ByteBuf heapOut = ctx.alloc().heapBuffer( cipher.getOutputSize( readableBytes ) );

        cipher( in, heapOut );
        return heapOut;
    }

    @Override
    public void free()
    {
        bufferOutLocal.get().clear();
    }

    @Override
    public boolean allowComposite()
    {
        return true;
    }
}
