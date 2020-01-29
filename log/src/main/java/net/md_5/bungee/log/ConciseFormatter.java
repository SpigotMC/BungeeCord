package net.md_5.bungee.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;

@RequiredArgsConstructor
public class ConciseFormatter extends Formatter
{

    private final DateFormat date = new SimpleDateFormat( System.getProperty( "net.md_5.bungee.log-date-format", "HH:mm:ss" ) );
    private final boolean coloured;

    @Override
    @SuppressWarnings("ThrowableResultIgnored")
    public String format(LogRecord record)
    {
        StringBuilder formatted = new StringBuilder();

        formatted.append( date.format( record.getMillis() ) );
        formatted.append( " [" );
        appendLevel( formatted, record.getLevel() );
        formatted.append( "] " );
        formatted.append( formatMessage( record ) );
        formatted.append( '\n' );

        if ( record.getThrown() != null )
        {
            StringWriter writer = new StringWriter();
            record.getThrown().printStackTrace( new PrintWriter( writer ) );
            formatted.append( writer );
        }

        return formatted.toString();
    }

    private void appendLevel(StringBuilder builder, Level level)
    {
        if ( !coloured )
        {
            builder.append( level.getLocalizedName() );
            return;
        }

        ChatColor color;

        if ( level == Level.INFO )
        {
            color = ChatColor.BLUE;
        } else if ( level == Level.WARNING )
        {
            color = ChatColor.YELLOW;
        } else if ( level == Level.SEVERE )
        {
            color = ChatColor.RED;
        } else
        {
            color = ChatColor.AQUA;
        }

        builder.append( color ).append( level.getLocalizedName() ).append( ChatColor.RESET );
    }
}
