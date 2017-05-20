package net.md_5.bungee;

import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.command.ConsoleCommandSender;

public class BungeeCordLauncher
{

    public static void main(String[] args) throws Exception
    {
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
                System.err.println( "*** Ну тут короче предупреждение, что банджа устарела ***" );
                System.err.println( "*** Скорее всего это так, а может и нет ***" );
                System.err.println( "*** Ищите новый билд у меня в сборке, если он конечно есть ***" );
                System.err.println( "*** Стартуем через 3 секунды ***" );
                Thread.sleep( TimeUnit.SECONDS.toMillis( 3 ) );
            }
        }

        BungeeCord bungee = new BungeeCord();
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
