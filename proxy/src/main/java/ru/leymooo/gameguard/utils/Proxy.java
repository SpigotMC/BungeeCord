package ru.leymooo.gameguard.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.gameguard.Config;

/**
 *
 * @author AuthMeReloaded Team
 */
public class Proxy
{

    /* Добро пожаловать в гору говнокода и костылей */
    private static final String PROXY_URL
            = "http://151.80.108.152/proxy.txt";
    //private static final ExecutorService executor = Executors.newFixedThreadPool( 4 );

    public HashSet<String> PROXIES = new HashSet<>();

    private static final Pattern IPADDRESS_PATTERN
            = Pattern.compile( "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$" );
    private Thread downloadTask;

    private final File dataFile;
    private final File dataFileAuto;

    public Proxy(File dataFolder)
    {
        this.dataFile = new File( dataFolder, "proxy.txt" );
        this.dataFileAuto = new File( dataFolder, "proxyAutoDetected.txt" );
        int tries = 0;
        while ( Config.getConfig().getGeoUtils().isDataAvailable() == false )
        {
            try
            {
                Thread.sleep( 1000L );
                if ( tries == 10 )
                {
                    System.out.println( "[GameGuard] Не удалось скачать GeoIp. Продолжаем без неё." );
                    break;
                }
                System.out.println( "[GameGuard] Ждём пока скачается GeoIp дата база." );
                tries++;
            } catch ( InterruptedException ex )
            {
            }
        }
        loadProxies( dataFile, false );
        loadProxies( dataFileAuto, true );
        downloadTask = updateProxiesFromSite();
        downloadTask.start();
    }

    private void loadProxies(File f, boolean force)
    {
        if ( !f.exists() )
        {
            try
            {
                f.createNewFile();
            } catch ( IOException e )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not create proxy file", e );
            }
            return;
        }

        try
        {
            Files.lines( Paths.get( f.getPath() ) ).forEach( line ->
            {
                try
                {
                    if ( force )
                    {
                        PROXIES.add( line );
                    } else
                    {
                        validateAndAddProxy( line, false );
                    }
                } catch ( IOException ex )
                {
                    BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not save proxy list", ex );
                }
            } );
        } catch ( IOException ex )
        {
            BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not read proxy list", ex );

        }
    }

    private void validateAndAddProxy(String line, boolean addToFile) throws IOException
    {
        String proxy = line.contains( ":" ) ? line.split( ":" )[0].trim() : line.trim();
        if ( IPADDRESS_PATTERN.matcher( proxy ).matches() && Config.getConfig().getGeoUtils().isAllowed( Config.getConfig().getGeoUtils().getCountryCode( proxy ), true ) )
        {
            if ( !PROXIES.contains( proxy ) )
            {
                PROXIES.add( proxy );
                if ( addToFile )
                {
                    Files.write( Paths.get( dataFile.getPath() ), Arrays.asList( proxy ), Charset.forName( "UTF-8" ), StandardOpenOption.APPEND );
                }
            }
        }
    }

    public void addProxyForce(String proxy)
    {
        if ( !isProxy( proxy ) )
        {
            PROXIES.add( proxy );
            try
            {
                Files.write( Paths.get( dataFileAuto.getPath() ), Arrays.asList( proxy ), Charset.forName( "UTF-8" ), StandardOpenOption.APPEND, StandardOpenOption.CREATE );
            } catch ( IOException ex )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not save proxy to file", ex );
            }
        }
    }

    private Thread updateProxiesFromSite()
    {
        return new Thread( () ->
        {
            try
            {
                URL url = new URL( PROXY_URL );
                BufferedReader in = new BufferedReader(
                        new InputStreamReader( url.openStream() ) );
                String inputLine;
                while ( ( inputLine = in.readLine() ) != null )
                {
                    validateAndAddProxy( inputLine, true );
                }
                in.close();
            } catch ( IOException e )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not download proxy list", e );
            }
        } );
    }

    public boolean isProxy(String ip)
    {
        return PROXIES.contains( ip );
    }

}
