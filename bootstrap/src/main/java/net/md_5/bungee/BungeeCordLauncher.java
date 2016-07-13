package net.md_5.bungee;

import java.io.PrintStream;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import jline.console.ConsoleReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.md_5.bungee.log.BungeeConsoleAppender;
import net.md_5.bungee.log.LoggingOutputStream;
import org.apache.logging.log4j.LogManager;

public class BungeeCordLauncher
{

    public static void main(String[] args) throws Exception
    {
        System.setProperty( "java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager" );
        System.setProperty( "Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector" );
        Security.setProperty( "networkaddress.cache.ttl", "30" );
        Security.setProperty( "networkaddress.cache.negative.ttl", "10" );

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.acceptsAll( Arrays.asList( "v", "version" ) );
        parser.acceptsAll( Arrays.asList( "noconsole" ) );

        OptionSet options = parser.parse( args );

        if ( options.has( "version" ) )
        {
            System.out.println( Bootstrap.class.getPackage().getImplementationVersion() );
            return;
        }

        if ( BungeeCord.class.getPackage().getSpecificationVersion() != null && System.getProperty( "IReallyKnowWhatIAmDoingISwear" ) == null )
        {
            Date buildDate = new SimpleDateFormat( "yyyyMMdd" ).parse( BungeeCord.class.getPackage().getSpecificationVersion() );

            Calendar deadline = Calendar.getInstance();
            deadline.add( Calendar.WEEK_OF_YEAR, -4 );
            if ( buildDate.before( deadline.getTime() ) )
            {
                System.err.println( "*** Warning, this build is outdated ***" );
                System.err.println( "*** Please download a new build from http://ci.md-5.net/job/BungeeCord ***" );
                System.err.println( "*** You will get NO support regarding this build ***" );
                System.err.println( "*** Server will start in 10 seconds ***" );
                Thread.sleep( TimeUnit.SECONDS.toMillis( 10 ) );
            }
        }

        // This is a workaround for quite possibly the weirdest bug I have ever encountered in my life!
        // When jansi attempts to extract its natives, by default it tries to extract a specific version,
        // using the loading class's implementation version. Normally this works completely fine,
        // however when on Windows certain characters such as - and : can trigger special behaviour.
        // Furthermore this behaviour only occurs in specific combinations due to the parsing done by jansi.
        // For example test-test works fine, but test-test-test does not! In order to avoid this all together but
        // still keep our versions the same as they were, we set the override property to the essentially garbage version
        // BungeeCord. This version is only used when extracting the libraries to their temp folder.
        System.setProperty( "library.jansi.version", "BungeeCord" );

        ConsoleReader consoleReader = new ConsoleReader();
        consoleReader.setExpandEvents( false );
        BungeeConsoleAppender.setConsoleReader( consoleReader );

        System.setErr( new PrintStream( new LoggingOutputStream( LogManager.getLogger( "SYSERR" ), org.apache.logging.log4j.Level.ERROR ), true ) );
        System.setOut( new PrintStream( new LoggingOutputStream( LogManager.getLogger( "SYSOUT" ), org.apache.logging.log4j.Level.INFO ), true ) );

        BungeeCord bungee = new BungeeCord( consoleReader );
        ProxyServer.setInstance( bungee );
        bungee.getLogger().info( "Enabled BungeeCord version " + bungee.getVersion() );
        bungee.start();

        if ( !options.has( "noconsole" ) )
        {
            String line;
            while ( bungee.isRunning && ( line = bungee.getConsoleReader().readLine( ">" ) ) != null )
            {
                if ( !bungee.getPluginManager().dispatchCommand( ConsoleCommandSender.getInstance(), line ) )
                {
                    bungee.getConsole().sendMessage( ChatColor.RED + "Command not found" );
                }
            }
        }
    }
}
