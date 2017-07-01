package net.md_5.bungee.log;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import jline.console.ConsoleReader;
import net.md_5.bungee.api.ChatColor;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Booleans;
import org.fusesource.jansi.Ansi;

@Plugin(name = "BungeeConsole", category = "Core", elementType = AbstractAppender.ELEMENT_TYPE, printObject = true)
public class BungeeConsoleAppender extends AbstractAppender
{

    private static final String ANSI_RESET = Ansi.ansi().reset().toString();
    private static final String ERASE_LINE = Ansi.ansi().eraseLine( Ansi.Erase.ALL ).toString() + ConsoleReader.RESET_LINE;
    private static final Map<String, String> COLOR_REPLACEMENTS = new HashMap<>();

    private static ConsoleReader reader;

    static
    {

        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.BLACK, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLACK ).boldOff().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.DARK_BLUE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLUE ).boldOff().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.DARK_GREEN, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.GREEN ).boldOff().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.DARK_AQUA, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.CYAN ).boldOff().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.DARK_RED, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.RED ).boldOff().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.DARK_PURPLE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.MAGENTA ).boldOff().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.GOLD, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.YELLOW ).boldOff().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.GRAY, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.WHITE ).boldOff().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.DARK_GRAY, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLACK ).bold().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.BLUE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLUE ).bold().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.GREEN, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.GREEN ).bold().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.AQUA, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.CYAN ).bold().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.RED, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.RED ).bold().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.LIGHT_PURPLE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.MAGENTA ).bold().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.YELLOW, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.YELLOW ).bold().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.WHITE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.WHITE ).bold().toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.MAGIC, Ansi.ansi().a( Ansi.Attribute.BLINK_SLOW ).toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.BOLD, Ansi.ansi().a( Ansi.Attribute.UNDERLINE_DOUBLE ).toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.STRIKETHROUGH, Ansi.ansi().a( Ansi.Attribute.STRIKETHROUGH_ON ).toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.UNDERLINE, Ansi.ansi().a( Ansi.Attribute.UNDERLINE ).toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.ITALIC, Ansi.ansi().a( Ansi.Attribute.ITALIC ).toString() );
        COLOR_REPLACEMENTS.put( "(?i)" + ChatColor.RESET, Ansi.ansi().a( Ansi.Attribute.RESET ).toString() );

    }

    protected BungeeConsoleAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout, final boolean ignoreExceptions)
    {
        super( name, filter, layout, ignoreExceptions );

    }

    @Override
    public void append(final LogEvent event)
    {

        if ( reader == null )
        {
            return;
        }

        String line = colorize( new String( getLayout().toByteArray( event ) ) );

        if ( !line.endsWith( "\n" ) )
        {
            line += "\n";
        }

        try
        {
            //reader.getOutput().write( ERASE_LINE + line + ANSI_RESET );
            reader.print( ERASE_LINE + line + ANSI_RESET );

            reader.drawLine();

            reader.flush();

        } catch ( IOException ex )
        {
            // ignore
        }

    }

    public static void setConsoleReader(final ConsoleReader r)
    {
        reader = r;
    }

    @PluginFactory
    public static BungeeConsoleAppender createAppender(
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filters") final Filter filter,
            @PluginAttribute("target") final String t,
            @PluginAttribute("name") final String name,
            @PluginAttribute("follow") final String follow,
            @PluginAttribute("ignoreExceptions") final String ignore)
    {

        if ( name == null )
        {
            LOGGER.error( "No name provided for BungeeConsoleAppender" );
            return null;
        }

        if ( layout == null )
        {
            return new BungeeConsoleAppender( name, filter, PatternLayout.newBuilder().build(), Booleans.parseBoolean( ignore, true ) );
        } else
        {
            return new BungeeConsoleAppender( name, filter, layout, Booleans.parseBoolean( ignore, true ) );
        }

    }

    private static String colorize(final String message)
    {

        if ( message.indexOf( ChatColor.COLOR_CHAR ) == -1 )
        {
            return message;
        }

        String result = message;

        for ( Map.Entry<String, String> entry : COLOR_REPLACEMENTS.entrySet() )
        {
            result = result.replaceAll( entry.getKey(), entry.getValue() );
        }

        return result;

    }

}
