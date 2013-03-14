package net.md_5.bungee;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logger to handle formatting and storage of the proxy's logger.
 */
public class BungeeLogger extends Logger
{

    private static final Formatter formatter = new ConsoleLogFormatter();
    static final BungeeLogger instance = new BungeeLogger();

    public BungeeLogger()
    {
        super( "BungeeCord", null );
        try
        {
            FileHandler handler = new FileHandler( "proxy.log", 1 << 14, 1, true );
            handler.setFormatter( formatter );
            addHandler( handler );
        } catch ( IOException ex )
        {
            System.err.println( "Could not register logger!" );
            ex.printStackTrace();
        }
    }

    @Override
    public void log(LogRecord record)
    {
        super.log( record );
        String message = formatter.format( record );
        if ( record.getLevel() == Level.SEVERE || record.getLevel() == Level.WARNING )
        {
            System.err.print( message );
        } else
        {
            System.out.print( message );
        }
    }

    public static class ConsoleLogFormatter extends Formatter
    {

        private SimpleDateFormat formatter = new SimpleDateFormat( "HH:mm:ss" );

        @Override
        public String format(LogRecord logrecord)
        {
            StringBuilder formatted = new StringBuilder();

            formatted.append( formatter.format( logrecord.getMillis() ) );
            Level level = logrecord.getLevel();

            if ( level == Level.FINEST )
            {
                formatted.append( " [FINEST] " );
            } else if ( level == Level.FINER )
            {
                formatted.append( " [FINER] " );
            } else if ( level == Level.FINE )
            {
                formatted.append( " [FINE] " );
            } else if ( level == Level.INFO )
            {
                formatted.append( " [INFO] " );
            } else if ( level == Level.WARNING )
            {
                formatted.append( " [WARNING] " );
            } else if ( level == Level.SEVERE )
            {
                formatted.append( " [SEVERE] " );
            }

            formatted.append( formatMessage( logrecord ) );
            formatted.append( '\n' );
            Throwable throwable = logrecord.getThrown();

            if ( throwable != null )
            {
                StringWriter writer = new StringWriter();

                throwable.printStackTrace( new PrintWriter( writer ) );
                formatted.append( writer );
            }

            return formatted.toString();
        }
    }
}
