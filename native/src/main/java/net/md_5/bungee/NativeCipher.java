package net.md_5.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

public class NativeCipher implements BungeeCipher
{
    private boolean forEncryption;
    private SecretKey key;
    private byte[] iv;

    @Getter
    private final NativeCipherImpl nativeCipher = new NativeCipherImpl();
    private static boolean loaded = false;


    public static boolean load()
    {
        if ( loaded ) return loaded;

        try
        {
            String file = "libbungeecord-native-1.0.so";
            InputStream lib = BungeeCipher.class.getClassLoader().getResourceAsStream("lib/amd64-Linux-gpp/jni/"+file);
            if ( lib == null )
            {
                NativeCipher.loaded = true;
                System.loadLibrary("bungeecord-native-1.0");
                return true; // ssssh, we are inside of cipher tests!
            }
            File dir = Files.createTempDirectory("bungee").toFile();
            OutputStream outputStream =
                    new FileOutputStream(new File(dir, file));

            int read;
            byte[] bytes = new byte[1024];

            while ((read = lib.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            System.load(new File(dir.getPath(), file).getPath());
            NativeCipher.loaded = true;
            return true;
        } catch (Throwable ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isLoaded()
    {
        return loaded;
    }

    @Override
    public void init(boolean forEncryption, SecretKey key) throws GeneralSecurityException {
        this.forEncryption = forEncryption;
        this.key = key;
        this.iv = key.getEncoded(); // initialize the IV
    }

    public void cipher(ByteBuf in, ByteBuf out) throws GeneralSecurityException {
        // Smoke tests
        in.memoryAddress();
        out.memoryAddress();
        // Store how many bytes we can cipher
        int length = in.readableBytes();
        // It is important to note that in AES CFB-8 mode, the number of read bytes, is the number of outputted bytes
        if ( out.writableBytes() < length )
        {
            out.capacity( length );
        }
        // Cipher the bytes
        nativeCipher.cipher(forEncryption, key.getEncoded(), iv, in.memoryAddress() + in.readerIndex(), out.memoryAddress() + out.writerIndex(), length);

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
        cipher(in, heapOut);

        return heapOut;
    }
}
