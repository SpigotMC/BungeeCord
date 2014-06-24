package net.md_5.bungee.log;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import jline.console.ConsoleReader;
import net.md_5.bungee.api.ChatColor;
import org.fusesource.jansi.Ansi;

public class ColouredWriter extends Handler
{

    private final Map<ChatColor, String> replacements = new EnumMap<>( ChatColor.class );
    private final ChatColor[] colors = ChatColor.values();
    private final ConsoleReader console;

    public ColouredWriter(ConsoleReader console)
    {
        this.console = console;

        replacements.put( ChatColor.BLACK, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLACK ).boldOff().toString() );
        replacements.put( ChatColor.DARK_BLUE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLUE ).boldOff().toString() );
        replacements.put( ChatColor.DARK_GREEN, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.GREEN ).boldOff().toString() );
        replacements.put( ChatColor.DARK_AQUA, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.CYAN ).boldOff().toString() );
        replacements.put( ChatColor.DARK_RED, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.RED ).boldOff().toString() );
        replacements.put( ChatColor.DARK_PURPLE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.MAGENTA ).boldOff().toString() );
        replacements.put( ChatColor.GOLD, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.YELLOW ).boldOff().toString() );
        replacements.put( ChatColor.GRAY, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.WHITE ).boldOff().toString() );
        replacements.put( ChatColor.DARK_GRAY, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLACK ).bold().toString() );
        replacements.put( ChatColor.BLUE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLUE ).bold().toString() );
        replacements.put( ChatColor.GREEN, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.GREEN ).bold().toString() );
        replacements.put( ChatColor.AQUA, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.CYAN ).bold().toString() );
        replacements.put( ChatColor.RED, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.RED ).bold().toString() );
        replacements.put( ChatColor.LIGHT_PURPLE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.MAGENTA ).bold().toString() );
        replacements.put( ChatColor.YELLOW, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.YELLOW ).bold().toString() );
        replacements.put( ChatColor.WHITE, Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.WHITE ).bold().toString() );
        replacements.put( ChatColor.MAGIC, Ansi.ansi().a( Ansi.Attribute.BLINK_SLOW ).toString() );
        replacements.put( ChatColor.BOLD, Ansi.ansi().a( Ansi.Attribute.UNDERLINE_DOUBLE ).toString() );
        replacements.put( ChatColor.STRIKETHROUGH, Ansi.ansi().a( Ansi.Attribute.STRIKETHROUGH_ON ).toString() );
        replacements.put( ChatColor.UNDERLINE, Ansi.ansi().a( Ansi.Attribute.UNDERLINE ).toString() );
        replacements.put( ChatColor.ITALIC, Ansi.ansi().a( Ansi.Attribute.ITALIC ).toString() );
        replacements.put( ChatColor.RESET, Ansi.ansi().a( Ansi.Attribute.RESET ).toString() );
    }

    public void print(String s)
    {
        for ( ChatColor color : colors )
        {
            s = s.replaceAll( "(?i)" + color.toString(), replacements.get( color ) );
        }
        try
        {
            console.print( ConsoleReader.RESET_LINE + s + Ansi.ansi().reset().toString() );
            console.drawLine();
            console.flush();
        } catch ( IOException ex )
        {
        }
    }

    @Override
    public void publish(LogRecord record)
    {
        if ( isLoggable( record ) )
        {
            print( getFormatter().format( record ) );
        }
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws SecurityException
    {
    }

}
