package ru.leymooo.botfilter.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.config.Configuration;
import ru.leymooo.botfilter.Config;

public class Proxy
{

    /* Добро пожаловать в гору говнокода и костылей */
    public HashSet<String> PROXIES = new HashSet<>();
    public HashSet<String> DOWNLOADED_URLS = new HashSet<>();

    //Для парса с сайтов
    private static final Pattern PARSE_IPPATTERN = Pattern.compile( "((\\d+\\.+){2,}\\d+)" );
    //Для проверки что спарсили
    private static final Pattern IPADDRESS_PATTERN
            = Pattern.compile( "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$" );
    private static Thread downloadTask;

    private File dataFile;
    private File dataFileAuto;

    private boolean enabled = true;
    private final Configuration proxySection;

    private static final Logger logger = BungeeCord.getInstance().getLogger();

    public Proxy(File dataFolder, Configuration section)
    {
        this.proxySection = section;
        if ( !section.getBoolean( "enabled" ) )
        {
            this.enabled = false;
            return;
        }
        this.dataFile = new File( dataFolder, "proxy.txt" );
        this.dataFileAuto = new File( dataFolder, "proxyAutoDetected.txt" );
        while ( Config.getConfig().getGeoUtils().isDownloading() )
        {
            try
            {
                Thread.sleep( 1000L );
                logger.log( Level.INFO, "[BotFilter] Ждём пока скачается GeoIp дата база." );
            } catch ( InterruptedException ex )
            {
            }
        }
        if ( !Config.getConfig().getGeoUtils().isAvailable() )
        {
            logger.log( Level.INFO, "[BotFilter] Не удалось скачать GeoIp. Продолжаем без неё." );
        }
        loadProxies( dataFile, false );
        loadProxies( dataFileAuto, true );
        logger.log( Level.INFO, "[BotFilter] Загружено {0} прокси из файлов!", PROXIES.size() );
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
                logger.log( Level.WARNING, "Could not create proxy file", e );
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
                    logger.log( Level.WARNING, "Could not save proxy to list", ex );
                }
            } );
        } catch ( IOException ex )
        {
            logger.log( Level.WARNING, "Could not read proxy list", ex );

        }
    }

    private void validateAndAddProxy(String line, boolean addToFile) throws IOException
    {
        String proxy = line.contains( ":" ) ? line.split( ":" )[0].trim() : line.trim();

        if ( IPADDRESS_PATTERN.matcher( proxy ).matches() && Config.getConfig().getGeoUtils().isAllowed( Config.getConfig().getGeoUtils().getCountryCode( InetAddress.getByName( proxy ) ), true ) )
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
        if ( !enabled )
        {
            return;
        }
        if ( !isProxy( proxy ) )
        {
            PROXIES.add( proxy );
            try
            {
                Files.write( Paths.get( dataFileAuto.getPath() ), Arrays.asList( proxy ), Charset.forName( "UTF-8" ), StandardOpenOption.APPEND, StandardOpenOption.CREATE );
            } catch ( IOException ex )
            {
                logger.log( Level.WARNING, "Could not save proxy to file", ex );
            }
        }
    }

    private Thread updateProxiesFromSite()
    {
        if ( downloadTask != null && downloadTask.isAlive() )
        {
            downloadTask.interrupt();
        }
        return new Thread( () ->
        {
            while ( !Thread.interrupted() )
            {
                if ( Config.getConfig().getGeoUtils().isAvailable() )
                {
                    logger.log( Level.INFO, "[BotFilter] Пытаюсь скачать прокси с сайтов.." );
                    int before = PROXIES.size();
                    List<String> urls = proxySection.getStringList( "download-list" );
                    for ( String site : urls )
                    {
                        try
                        {
                            getProxyFromPage( site );
                        } catch ( IOException e )
                        {
                            logger.log( Level.WARNING, "Не могу скачать прокси с сайта {0}. Причина: {1}", new Object[]
                            {
                                site, e.getMessage()
                            } );
                            logger.log( Level.WARNING, "Скачиваю прокси со следующего сайта!" );
                        }
                    }
                    urls = proxySection.getStringList( "blogspot-proxy" );
                    for ( String site : urls )
                    {
                        String[] regexSplitted = site.split( ";" );
                        String regex = regexSplitted.length == 2 ? regexSplitted[1] : "href=\'(.*?)\'";
                        List<String> list = getHRefs( regexSplitted[0], regex );
                        for ( String pages : list )
                        {
                            if ( pages.contains( "/20" ) && !pages.contains( "#" ) )
                            {
                                try
                                {
                                    getProxyFromPage( pages );
                                } catch ( IOException e )
                                {
                                    logger.log( Level.WARNING, "Не могу скачать прокси с сайта {0}. Причина: {1}", new Object[]
                                    {
                                        site, e.getMessage()
                                    } );
                                    logger.log( Level.WARNING, "Скачиваю прокси со следующего сайта!" );
                                }
                            }
                        }
                    }
                    logger.log( Level.INFO, "[BotFilter] Скачано {0} прокси с сайтов!", ( PROXIES.size() - before ) );
                } else
                {
                    logger.log( Level.WARNING, "[BotFilter] Не могу скачать прокси. GeoIp недоступен" );
                }
                try
                {
                    Thread.sleep( 1000 * 60 * 60 * 4 );//4 hours
                    if ( !Config.getConfig().getGeoUtils().isAvailable() )
                    {
                        logger.log( Level.INFO, "[BotFilter] Пытаюсь скачать GeoIp" );
                        Config.getConfig().getGeoUtils().createDownloadTask().run();
                    }
                } catch ( InterruptedException ex )
                {
                    return;
                }
            }
        } );

    }

    private void getProxyFromPage(String page) throws IOException
    {
        if ( DOWNLOADED_URLS.contains( page ) )
        {
            return;
        }
        DOWNLOADED_URLS.add( page );
        URL url = new URL( page );
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout( 3000 );
        try ( BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) ) )
        {
            String inputLine;
            while ( ( inputLine = in.readLine() ) != null )
            {
                Matcher matcher = PARSE_IPPATTERN.matcher( inputLine );
                while ( matcher.find() )
                {
                    validateAndAddProxy( matcher.group(), true );
                }
            }
        }
    }

    public boolean isProxy(String ip)
    {
        if ( !enabled )
        {
            return false;
        }
        return PROXIES.contains( ip );
    }

    /* AntiBot-ultra code*/
    public List<String> getHRefs(String url1, String regexPattern)
    {
        List<String> list = new ArrayList<>();
        try
        {
            StringBuilder sb = new StringBuilder();
            Pattern pattern = Pattern.compile( regexPattern, Pattern.DOTALL );
            URL url = new URL( url1 );
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout( 3000 );
            try ( Scanner s = new Scanner( conn.getInputStream() ) )
            {
                while ( s.hasNext() )
                {
                    sb.append( s.nextLine() );
                }
                Matcher matcher = pattern.matcher( sb.toString() );
                while ( matcher.find() )
                {
                    list.add( matcher.group( 1 ) );
                }
            }
        } catch ( IOException e )
        {
            logger.log( Level.WARNING, "Не могу скачать прокси с сайта {0}. Причина: {1}", new Object[]
            {
                url1, e.getMessage()
            } );
            logger.log( Level.WARNING, "Скачиваю прокси со следующего сайта!" );
        }
        return list;
    }

}
