package net.md_5.bungee.jni;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.md_5.bungee.jni.cipher.BungeeCipher;

public final class NativeCode<T>
{

    private final String name;
    private final Class<T> javaImpl;
    private final Class<T> nativeImpl;
    //
    private boolean loaded;

    public NativeCode(String name, Class<T> javaImpl, Class<T> nativeImpl)
    {
        this.name = name;
        this.javaImpl = javaImpl;
        this.nativeImpl = nativeImpl;
    }

    public T newInstance()
    {
        try
        {
            return ( loaded ) ? nativeImpl.newInstance() : javaImpl.newInstance();
        } catch ( IllegalAccessException | InstantiationException ex )
        {
            throw new RuntimeException( "Error getting instance", ex );
        }
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
                String unsatisfiedLinkError = null;
                for ( String version : new String[]{ "1.1", "1.0.0", "" } ) // Hardcoded, but seem better than scanning jar content
                {
                    String fileName = name + ".so" + ( !version.isEmpty() ? "." + version : "" );
                    try ( InputStream soFile = BungeeCipher.class.getClassLoader().getResourceAsStream( fileName ) )
                    {
                        if ( soFile == null ) continue;

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
                        unsatisfiedLinkError = "Could not load native library: " + ex.getMessage();
                    }
                }
                if ( !loaded && unsatisfiedLinkError != null )
                {
                    System.out.println( unsatisfiedLinkError );
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
