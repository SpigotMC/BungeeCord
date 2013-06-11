package net.md_5.bungee.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.md_5.bungee.BungeeCord;

public class BungeeLogger extends Logger
{

    private final BungeeCord bungee;
    private final ColouredWriter writer;
    private final Formatter formatter = new ConciseFormatter();
    private final LogDispatcher dispatcher = new LogDispatcher( this );

    public BungeeLogger(BungeeCord bungee)
    {
        super( "BungeeCord", null );
        this.bungee = bungee;
        this.writer = new ColouredWriter( bungee.getConsoleReader() );

        try
        {
            FileHandler handler = new FileHandler( "proxy.log", 1 << 24, 8, true );
            handler.setFormatter( formatter );
            addHandler( handler );
        } catch ( IOException ex )
        {
            System.err.println( "Could not register logger!" );
            ex.printStackTrace();
        }
        dispatcher.start();
    }

    @Override
    public void log(LogRecord record)
    {
        dispatcher.queue( record );
    }

    void doLog(LogRecord record)
    {
        super.log( record );
        writer.print( formatter.format( record ) );
    }
}
