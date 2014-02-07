package net.md_5.bungee;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.command.ConsoleCommandSender;

public class Bootstrap
{

    private static List<String> list(String... params)
    {
        return Arrays.asList( params );
    }

    /**
     * Starts a new instance of BungeeCord.
     *
     * @param args command line arguments, currently none are used
     * @throws Exception when the server cannot be started
     */
    public static void main(String[] args) throws Exception
    {
        if ( Float.parseFloat( System.getProperty( "java.class.version" ) ) < 51.0 )
        {
            System.err.println( "*** ERROR *** BungeeCord requires Java 7 or above to function!" );
            return;
        }

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.acceptsAll( list( "v", "version" ) );

        OptionSet options = parser.parse( args );

        if ( options.has( "version" ) )
        {
            System.out.println( Bootstrap.class.getPackage().getImplementationVersion() );
            return;
        }

        if ( BungeeCord.class.getPackage().getSpecificationVersion() != null && System.getProperty( "IReallyKnowWhatIAmDoingISwear" ) == null )
        {
            Calendar deadline = Calendar.getInstance();
            deadline.add( Calendar.WEEK_OF_YEAR, 2 );
            if ( Calendar.getInstance().after( new SimpleDateFormat( "yyyyMMdd" ).parse( BungeeCord.class.getPackage().getSpecificationVersion() ) ) )
            {
                System.err.println( "*** Warning, this build is outdated ***" );
                System.err.println( "*** Please download a new build from http://ci.md-5.net/job/BungeeCord ***" );
                System.err.println( "*** You will get NO support regarding this build ***" );
                System.err.println( "*** Server will start in 30 seconds ***" );
                Thread.sleep( TimeUnit.SECONDS.toMillis( 30 ) );
            }
        }

        BungeeCord bungee = new BungeeCord();
        ProxyServer.setInstance( bungee );
        bungee.getLogger().info( "Enabled BungeeCord version " + bungee.getVersion() );
        bungee.start();

        while ( bungee.isRunning )
        {
            String line = bungee.getConsoleReader().readLine( ">" );
            if ( line != null )
            {
                if ( !bungee.getPluginManager().dispatchCommand( ConsoleCommandSender.getInstance(), line ) )
                {
                    bungee.getConsole().sendMessage( ChatColor.RED + "Command not found" );
                }
            }
        }
    }
}
