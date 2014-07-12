package net.md_5.bungee.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ConciseFormatter extends Formatter
{

    private final DateFormat date = new SimpleDateFormat( System.getProperty( "net.md_5.bungee.log-date-format", "HH:mm:ss" ) );

    @Override
    @SuppressWarnings("ThrowableResultIgnored")
    public String format(LogRecord record)
    {
        StringBuilder formatted = new StringBuilder();

        formatted.append( date.format( record.getMillis() ) );
        formatted.append( " [" );
        formatted.append( record.getLevel().getLocalizedName() );
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
}
