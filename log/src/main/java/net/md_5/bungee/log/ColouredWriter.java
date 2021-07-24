package net.md_5.bungee.log;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import jline.console.ConsoleReader;
import net.md_5.bungee.api.ChatColor;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Erase;

public class ColouredWriter extends Handler
{

    private static final HashMap<Character, String> COLOR_CODE_TO_ANSI = new HashMap<>();

    static
    {
        COLOR_CODE_TO_ANSI.put( ChatColor.BLACK.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLACK ).boldOff().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.DARK_BLUE.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLUE ).boldOff().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.DARK_GREEN.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.GREEN ).boldOff().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.DARK_AQUA.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.CYAN ).boldOff().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.DARK_RED.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.RED ).boldOff().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.DARK_PURPLE.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.MAGENTA ).boldOff().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.GOLD.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.YELLOW ).boldOff().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.GRAY.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.WHITE ).boldOff().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.DARK_GRAY.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLACK ).bold().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.BLUE.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLUE ).bold().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.GREEN.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.GREEN ).bold().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.AQUA.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.CYAN ).bold().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.RED.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.RED ).bold().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.LIGHT_PURPLE.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.MAGENTA ).bold().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.YELLOW.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.YELLOW ).bold().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.WHITE.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.WHITE ).bold().toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.MAGIC.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.BLINK_SLOW ).toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.BOLD.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.UNDERLINE_DOUBLE ).toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.STRIKETHROUGH.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.STRIKETHROUGH_ON ).toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.UNDERLINE.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.UNDERLINE ).toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.ITALIC.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.ITALIC ).toString() );
        COLOR_CODE_TO_ANSI.put( ChatColor.RESET.toString().charAt( 1 ), Ansi.ansi().a( Ansi.Attribute.RESET ).toString() );
    }

    private static final String formatAnsi(String msg)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for ( int index = 0, len = msg.length(); index < len; index++ )
        {
            char currentChar = msg.charAt( index );
            if ( currentChar == '§' )
            {
                char predictedColorCode = msg.charAt( ++index );
                String ansi = COLOR_CODE_TO_ANSI.get( predictedColorCode );
                if ( ansi != null )
                {
                    stringBuilder.append( ansi );
                } else
                {
                    stringBuilder.append( '§' ).append( predictedColorCode );
                }
            } else
            {
                stringBuilder.append( currentChar );
            }
        }

        return stringBuilder.toString();
    }
    //
    private final ConsoleReader console;

    public ColouredWriter(ConsoleReader console)
    {
        this.console = console;
    }

    public void print(String s)
    {
        try
        {
            console.print( Ansi.ansi().eraseLine( Erase.ALL ).toString() + ConsoleReader.RESET_LINE + formatAnsi( s ) + Ansi.ansi().reset().toString() );
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
