package net.md_5.bungee;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;

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

    public static String csv(Iterable<?> objects)
    {
        return format( objects, ", " );
    }

    public static String format(Iterable<?> objects, String separators)
    {
        return Joiner.on( separators ).join( objects );
    }

    public static String stupify(String text)
    {
        List<JsonObject> sections = new ArrayList<>();
        char[] c = text.toCharArray();

        char currentChar = 0x00;
        StringBuilder buffer = new StringBuilder();

        for ( int i = 0; i < text.length(); i++ )
        {
            if ( c[i] == ChatColor.COLOR_CHAR && ChatColor.ALL_CODES.indexOf( c[i + 1] ) != -1 )
            {
                sections.add( generateAndReset( currentChar, buffer ) );
                currentChar = Character.toLowerCase( c[++i] );
            } else
            {
                buffer.append( c[i] );
            }
        }
        sections.add( generateAndReset( currentChar, buffer ) );

        return BungeeCord.getInstance().gson.toJson( sections );
    }

    private static JsonObject generateAndReset(char currentChar, StringBuilder buffer)
    {
        JsonObject entry = new JsonObject();
        ChatColor colour = ChatColor.getByChar( currentChar );
        if ( colour != null )
        {
            entry.addProperty( "color", colour.getName() );
        }
        entry.addProperty( "text", buffer.toString() );

        buffer.setLength( 0 );
        return entry;
    }
}
