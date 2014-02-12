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
    private final static NativeCipherImpl nativeCipher = new NativeCipherImpl();
    private static boolean loaded;
    /*============================================================================*/
    private boolean forEncryption;
    private long keyPointer;
    private long ivPointer;

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
                try ( OutputStream outputStream = new FileOutputStream( temp ) )
                {
                    ByteStreams.copy( lib, outputStream );
                    System.load( temp.getPath() );
                }
                loaded = true;
            } catch ( Throwable t )
            {
                t.printStackTrace();
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
        nativeCipher.free( this.keyPointer, this.ivPointer );
        this.forEncryption = forEncryption;

        byte[] encoded = key.getEncoded();
        this.keyPointer = nativeCipher.initKey( encoded );
        this.ivPointer = nativeCipher.initIV( encoded );
    }

    @Override
    public void free()
    {
        nativeCipher.free( keyPointer, ivPointer );
    }

    @Override
    public void cipher(ByteBuf in, ByteBuf out) throws GeneralSecurityException
    {
        // Smoke tests
        in.memoryAddress();
        out.memoryAddress();
        Preconditions.checkState( keyPointer != 0, "Invalid pointer to AES key!" );
        Preconditions.checkState( ivPointer != 0, "Invalid pointer to IV!" );

        // Store how many bytes we can cipher
        int length = in.readableBytes();
        // It is important to note that in AES CFB-8 mode, the number of read bytes, is the number of outputted bytes
        out.ensureWritable( length );

        // Cipher the bytes
        nativeCipher.cipher( forEncryption, keyPointer, ivPointer, in.memoryAddress() + in.readerIndex(), out.memoryAddress() + out.writerIndex(), length );

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
