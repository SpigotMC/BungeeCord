package net.md_5.bungee.jni.cipher;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import lombok.Getter;

public class NativeCipher implements BungeeCipher
{

    @Getter
    private final NativeCipherImpl nativeCipher = new NativeCipherImpl();
    /*============================================================================*/
    private long ctx;

    @Override
    public void init(boolean forEncryption, SecretKey key) throws GeneralSecurityException
    {
        Preconditions.checkArgument( key.getEncoded().length == 16, "Invalid key size" );
        free();

        this.ctx = nativeCipher.init( forEncryption, key.getEncoded() );
    }

    @Override
    public void free()
    {
        if ( ctx != 0 )
        {
            nativeCipher.free( ctx );
            ctx = 0;
        }
    }

    @Override
    public void cipher(ByteBuf in, ByteBuf out) throws GeneralSecurityException
    {
        // Smoke tests
        in.memoryAddress();
        out.memoryAddress();
        Preconditions.checkState( ctx != 0, "Invalid pointer to AES key!" );

        // Store how many bytes we can cipher
        int length = in.readableBytes();
        // Older OpenSSL versions will flip if length <= 0
        if ( length <= 0 )
        {
            return;
        }

        // It is important to note that in AES CFB-8 mode, the number of read bytes, is the number of outputted bytes
        out.ensureWritable( length );

        // Cipher the bytes
        nativeCipher.cipher( ctx, in.memoryAddress() + in.readerIndex(), out.memoryAddress() + out.writerIndex(), length );

        // Go to the end of the buffer, all bytes would of been read
        in.readerIndex( in.writerIndex() );
        // Add the number of ciphered bytes to our position
        out.writerIndex( out.writerIndex() + length );
    }

    @Override
    public ByteBuf cipher(ChannelHandlerContext ctx, ByteBuf in) throws GeneralSecurityException
    {
        int readableBytes = in.readableBytes();
        ByteBuf heapOut = ctx.alloc().directBuffer( readableBytes ); // CFB8
        cipher( in, heapOut );

        return heapOut;
    }
}
