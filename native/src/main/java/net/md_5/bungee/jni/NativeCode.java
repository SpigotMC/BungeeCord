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
    private final boolean enableNativeFlag;
    private final boolean extendedSupportCheck;
    //
    private boolean loaded;

    public NativeCode(String name, Supplier<? extends T> javaImpl, Supplier<? extends T> nativeImpl)
    {
        this( name, javaImpl, nativeImpl, false );
    }

    public NativeCode(String name, Supplier<? extends T> javaImpl, Supplier<? extends T> nativeImpl, boolean extendedSupportCheck)
    {
        this.name = name;
        this.javaImpl = javaImpl;
        this.nativeImpl = nativeImpl;
        this.enableNativeFlag = Boolean.parseBoolean( System.getProperty( "net.md_5.bungee.jni." + name + ".enable", "true" ) );
        this.extendedSupportCheck = extendedSupportCheck;
    }

    public T newInstance()
    {
        return ( loaded ) ? nativeImpl.get() : javaImpl.get();
    }

    public boolean load()
    {
        if ( enableNativeFlag && !loaded && isSupported() )
        {
            String name = this.name + ( isAarch64() ? "-arm" : "" );
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

                    if ( extendedSupportCheck )
                    {
                        // Should throw NativeCodeException if incompatible
                        nativeImpl.get();
                    }

                    loaded = true;
                } catch ( IOException ex )
                {
                    // Can't write to tmp?
                } catch ( UnsatisfiedLinkError ex )
                {
                    System.out.println( "Could not load native library: " + ex.getMessage() );
                } catch ( NativeCodeException ex )
                {
                    System.out.println( "Native library " + name + " is incompatible: " + ex.getMessage() );
                }
            }
        }

        return loaded;
    }

    public static boolean isSupported()
    {
        return "Linux".equals( System.getProperty( "os.name" ) ) && ( isAmd64() || isAarch64() );
    }

    private static boolean isAmd64()
    {
        return "amd64".equals( System.getProperty( "os.arch" ) );
    }

    private static boolean isAarch64()
    {
        return "aarch64".equals( System.getProperty( "os.arch" ) );
    }
}
