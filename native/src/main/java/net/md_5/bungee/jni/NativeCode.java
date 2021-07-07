package net.md_5.bungee.jni;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;
import net.md_5.bungee.jni.cipher.BungeeCipher;

public final class NativeCode<T>
{

    private final String name;
    private final Supplier<? extends T> javaImpl;
    private final Supplier<? extends T> nativeImpl;
    //
    private boolean loaded;

    public NativeCode(String name, Supplier<? extends T> javaImpl, Supplier<? extends T> nativeImpl)
    {
        this.name = name;
        this.javaImpl = javaImpl;
        this.nativeImpl = nativeImpl;
    }

    public T newInstance()
    {
        return ( loaded ) ? nativeImpl.get() : javaImpl.get();
    }

    public boolean load()
    {
        if ( !loaded && isSupported() )
        {
            String fullName = "bungeecord-" + name;

            try
            {
                System.loadLibrary( fullName );
                loaded = true;
            } catch ( Throwable t )
            {
            }

            if ( !loaded )
            {
                try ( InputStream soFile = BungeeCipher.class.getClassLoader().getResourceAsStream( name + ".so" ) )
                {
                    // Else we will create and copy it to a temp file
                    File temp = File.createTempFile( fullName, ".so" );
                    // Don't leave cruft on filesystem
                    temp.deleteOnExit();

                    try ( OutputStream outputStream = new FileOutputStream( temp ) )
                    {
                        ByteStreams.copy( soFile, outputStream );
                    }

                    System.load( temp.getPath() );
                    loaded = true;
                } catch ( IOException ex )
                {
                    // Can't write to tmp?
                } catch ( UnsatisfiedLinkError ex )
                {
                    System.err.println( "Could not load native library: " + ex.getMessage() );
                }
            }
        }

        return loaded;
    }

    public static boolean isSupported()
    {
        return "Linux".equals( System.getProperty( "os.name" ) ) && "amd64".equals( System.getProperty( "os.arch" ) );
    }
}
