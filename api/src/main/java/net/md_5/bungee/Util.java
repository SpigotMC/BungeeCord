package net.md_5.bungee;

import com.google.common.base.Joiner;
import com.google.common.primitives.UnsignedLongs;
import io.netty.channel.unix.DomainSocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.UUID;

/**
 * Series of utility classes to perform various operations.
 */
public class Util
{

    public static final int DEFAULT_PORT = 25565;

    /**
     * Method to transform human readable addresses into usable address objects.
     *
     * @param hostline in the format of 'host:port'
     * @return the constructed hostname + port.
     */
    public static SocketAddress getAddr(String hostline)
    {
        URI uri = null;
        try
        {
            uri = new URI( hostline );
        } catch ( URISyntaxException ex )
        {
        }

        if ( uri != null && "unix".equals( uri.getScheme() ) )
        {
            return new DomainSocketAddress( uri.getPath() );
        }

        if ( uri == null || uri.getHost() == null )
        {
            try
            {
                uri = new URI( "tcp://" + hostline );
            } catch ( URISyntaxException ex )
            {
                throw new IllegalArgumentException( "Bad hostline: " + hostline, ex );
            }
        }

        if ( uri.getHost() == null )
        {
            throw new IllegalArgumentException( "Invalid host/address: " + hostline );
        }

        return new InetSocketAddress( uri.getHost(), ( uri.getPort() ) == -1 ? DEFAULT_PORT : uri.getPort() );
    }

    /**
     * Formats an integer as a hex value.
     *
     * @param i the integer to format
     * @return the hex representation of the integer
     */
    public static String hex(int i)
    {
        return String.format( "0x%02X", i );
    }

    /**
     * Formats an char as a unicode value.
     *
     * @param c the character to format
     * @return the unicode representation of the character
     */
    public static String unicode(char c)
    {
        return "\\u" + String.format( "%04x", (int) c ).toUpperCase( Locale.ROOT );
    }

    /**
     * Constructs a pretty one line version of a {@link Throwable}. Useful for
     * debugging.
     *
     * @param t the {@link Throwable} to format.
     * @return a string representing information about the {@link Throwable}
     */
    public static String exception(Throwable t)
    {
        return exception( t, true );
    }

    /**
     * Constructs a pretty one line version of a {@link Throwable}. Useful for
     * debugging.
     *
     * @param t the {@link Throwable} to format.
     * @param includeLineNumbers whether to include line numbers
     * @return a string representing information about the {@link Throwable}
     */
    public static String exception(Throwable t, boolean includeLineNumbers)
    {
        // TODO: We should use clear manually written exceptions
        StackTraceElement[] trace = t.getStackTrace();
        return t.getClass().getSimpleName() + " : " + t.getMessage()
                + ( ( includeLineNumbers && trace.length > 0 ) ? " @ " + t.getStackTrace()[0].getClassName() + ":" + t.getStackTrace()[0].getLineNumber() : "" );
    }

    public static String csv(Iterable<?> objects)
    {
        return format( objects, ", " );
    }

    public static String format(Iterable<?> objects, String separators)
    {
        return Joiner.on( separators ).join( objects );
    }

    /**
     * Converts a String to a UUID
     *
     * @param uuid The string to be converted
     * @return The result
     */
    public static UUID getUUID(String uuid)
    {
        return new UUID( UnsignedLongs.parseUnsignedLong( uuid.substring( 0, 16 ), 16 ), UnsignedLongs.parseUnsignedLong( uuid.substring( 16 ), 16 ) );
    }
}
