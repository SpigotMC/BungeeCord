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
    private boolean forEncryption;
    private byte[] iv;
    /*============================================================================*/
    private static boolean loaded;

    private long pointer;

    public static boolean isSupported()
    {
        return getNativeLib() != null;
    }

    public static String getNativeLib() {
        String os = System.getProperty( "os.name" );
        String arch = System.getProperty( "os.arch" );
        String version = System.getProperty( "os.version" );

        if ( os.equals( "Linux" ) && arch.equals( "amd64" ) ) {
            return "native-cipher.so";
        } else if ( os.equals( "FreeBSD" ) && arch.equals( "amd64" ) ) {
            if ( version.startsWith( "9." ) ) {
                return "native-cipher-freebsd9.so";
            } else if ( version.startsWith( "10." ) ) {
                return "native-cipher-freebsd10.so";
            }
        }

        return null;
    }

    public static boolean load()
    {
        String nativeLib = getNativeLib();
        if ( !loaded && nativeLib != null )
        {
            try ( InputStream lib = BungeeCipher.class.getClassLoader().getResourceAsStream( nativeLib ) )
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
        if ( pointer != 0 )
        {
            nativeCipher.free( pointer );
        }
        this.forEncryption = forEncryption;
        this.iv = key.getEncoded(); // initialize the IV
        this.pointer = nativeCipher.init( key.getEncoded() );
    }

    @Override
    public void free()
    {
        if ( pointer != 0 )
        {
            nativeCipher.free( pointer );
            pointer = 0;
        }
    }

    @Override
    public void cipher(ByteBuf in, ByteBuf out) throws GeneralSecurityException
    {
        // Smoke tests
        in.memoryAddress();
        out.memoryAddress();
        Preconditions.checkState( pointer != 0, "Invalid pointer to AES key!" );
        Preconditions.checkState( iv != null, "Invalid IV!" );

        // Store how many bytes we can cipher
        int length = in.readableBytes();
        // It is important to note that in AES CFB-8 mode, the number of read bytes, is the number of outputted bytes
        out.ensureWritable( length );

        // Cipher the bytes
        nativeCipher.cipher( forEncryption, pointer, iv, in.memoryAddress() + in.readerIndex(), out.memoryAddress() + out.writerIndex(), length );

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
