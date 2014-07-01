package net.md_5.bungee;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public class NativeCipher implements BungeeCipher
{

    @Getter
    private final NativeCipherImpl nativeCipher = new NativeCipherImpl();
    /*============================================================================*/
    private static boolean loaded;
    private long ctx;

    public static boolean isSupported()
    {
        return "Linux".equals( System.getProperty( "os.name" ) ) && "amd64".equals( System.getProperty( "os.arch" ) );
    }

    public static boolean load()
    {
        if ( !loaded && isSupported() )
        {
            try ( InputStream lib = BungeeCipher.class.getClassLoader().getResourceAsStream( "native-cipher.so" ) )
            {
                // Else we will create and copy it to a temp file
                File temp = File.createTempFile( "bungeecord-native-cipher", ".so" );
                temp.deleteOnExit();

                try ( OutputStream outputStream = new FileOutputStream( temp ) )
                {
                    ByteStreams.copy( lib, outputStream );
                    System.load( temp.getPath() );
                }
                loaded = true;
            } catch ( Throwable t )
            {
            }
        }

        return loaded;
    }

    public static boolean isLoaded()
    {
        return loaded;
    }

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
