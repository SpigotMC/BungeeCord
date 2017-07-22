package net.md_5.bungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.command.ConsoleCommandSender;

public class BungeeCordLauncher
{

    private static int VERSION = 11;

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
        //GameGuard start
        if ( System.getProperty( "IReallyKnowWhatIAmDoingISwear" ) == null && checkUpdate() )
        {

            System.err.println( "*** ВНИМАНИЕ! Найдена новая версия***" );
            System.err.println( "*** Новая версия тут: ***" );
            System.err.println( "*** http://www.rubukkit.org/threads/137038/ ***" );
            System.err.println( "*** Рекомендую обновиться. ***" );
            System.err.println( "*** Запуск через 6 секунд ***" );
            Thread.sleep( TimeUnit.SECONDS.toMillis( 6 ) );
        }
        //GameGuard end

        BungeeCord bungee = new BungeeCord();
        ProxyServer.setInstance( bungee );
        bungee.getLogger().info( "Включаю BungeCord GameGuard от vk.com/Leymooo_s" );//GameGuard
        bungee.start();

        if ( !options.has( "noconsole" ) )
        {
            String line;
            while ( bungee.isRunning && ( line = bungee.getConsoleReader().readLine( ">" ) ) != null )
            {
                if ( !bungee.getPluginManager().dispatchCommand( ConsoleCommandSender.getInstance(), line ) )
                {
                    bungee.getConsole().sendMessage( ChatColor.RED + "Команда не найдена :(" ); //GameGuard
                }
            }
        }
    }

    private static boolean checkUpdate()
    {
        try
        {
            System.out.println( "[GameGuard] Проверяю наличее обновлений" );
            //Да да. В главном потоке)
            URL url = new URL( "http://151.80.108.152/gg-version.txt" );
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout( 3000 );
            conn.connect();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader( conn.getInputStream() ) );
            int version = Integer.parseInt( in.readLine() );
            in.close();
            return version != VERSION;
        } catch ( IOException | NumberFormatException ex )
        {
            System.err.println( "[GameGuard] Не могу проверить обновление" );
        }
        return false;
    }
}
