
import java.io.PrintStream;
import jline.console.ConsoleReader;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.md_5.bungee.log.BungeeConsoleAppender;
import net.md_5.bungee.log.LoggingOutputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author michael
 */
public class Test
{

    public static void main(String[] args) throws Exception
    {
        System.setProperty( "java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager" );
        System.setProperty( "Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector" );
        System.setProperty( "library.jansi.version", "BungeeCord" );

        ConsoleReader consoleReader = new ConsoleReader();
        consoleReader.setExpandEvents( false );
        BungeeConsoleAppender.setConsoleReader( consoleReader );

        System.setErr( new PrintStream( new LoggingOutputStream( LogManager.getLogger( "SYSERR" ), Level.ERROR ), true ) );
        System.setOut( new PrintStream( new LoggingOutputStream( LogManager.getLogger( "SYSOUT" ), Level.INFO ), true ) );

        BungeeCord bungee = new BungeeCord( consoleReader );
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
