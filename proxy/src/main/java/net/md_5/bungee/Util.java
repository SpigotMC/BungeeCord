package net.md_5.bungee;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Series of utility classes to perform various operations.
 */
public class Util
{

    private static final int DEFAULT_PORT = 25565;

    /**
     * Method to transform human readable addresses into usable address objects.
     *
     * @param hostline in the format of 'host:port'
     * @return the constructed hostname + port.
     */
    public static InetSocketAddress getAddr(String hostline)
    {
        String[] split = hostline.split( ":" );
        int port = DEFAULT_PORT;
        if ( split.length > 1 )
        {
            port = Integer.parseInt( split[1] );
        }
        return new InetSocketAddress( split[0], port );
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
     * Constructs a pretty one line version of a {@link Throwable}. Useful for
     * debugging.
     *
     * @param t the {@link Throwable} to format.
     * @return a string representing information about the {@link Throwable}
     */
    public static String exception(Throwable t)
    {
        // TODO: We should use clear manually written exceptions
        StackTraceElement[] trace = t.getStackTrace();
        return t.getClass().getSimpleName() + " : " + t.getMessage()
                + ( ( trace.length > 0 ) ? " @ " + t.getStackTrace()[0].getClassName() + ":" + t.getStackTrace()[0].getLineNumber() : "" );
    }

    public static String csv(Collection<?> objects)
    {
        return format( objects, ", " );
    }

    public static String format(Collection<?> objects, String separators)
    {
        StringBuilder ret = new StringBuilder();
        for ( Object o : objects )
        {
            ret.append( o );
            ret.append( separators );
        }

        return ( ret.length() == 0 ) ? "" : ret.substring( 0, ret.length() - separators.length() );
    }
}
