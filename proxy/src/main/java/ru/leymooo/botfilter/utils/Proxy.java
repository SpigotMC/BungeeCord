package ru.leymooo.botfilter.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javolution.util.FastSet;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.botfilter.BotFilter;
import ru.leymooo.botfilter.config.Settings;

/**
 *
 * @author Leymooo
 */
public class Proxy
{

    @Getter
    private boolean enabled = Settings.IMP.GEO_IP.MODE != 2 && Settings.IMP.PROXY.MODE != 2;

    private final Logger logger = BungeeCord.getInstance().getLogger();

    private FastSet<String> proxies = new FastSet<>();

    private Path proxyFile;
    private Path proxyFailedFile;
    private static final Pattern IPADDRESS_PATTERN
            = Pattern.compile( "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])" );

    private Thread downloadThread;

    public Proxy()
    {
        if ( enabled && ( enabled = BotFilter.getInstance().getGeoIp().isAvailable() ) )
        {
            loadProxyFromFile( proxyFile = Paths.get( "BotFilter" + File.separatorChar + "proxy.txt" ) );
            loadProxyFromFile( proxyFailedFile = Paths.get( "BotFilter" + File.separatorChar + "proxy-failed.txt" ) );
            logger.log( Level.INFO, "[BotFilter] Загружено {0} прокси из файлов.", proxies.size() );
            downloadProxyFromSites();
        }
    }

    public boolean isProxy(InetAddress address)
    {
        return proxies.contains( address.getHostAddress() );
    }

    public void addIp(String ip)
    {
        if ( enabled && !proxies.contains( ip ) )
        {
            proxies.add( ip );
            try
            {
                Files.write( proxyFailedFile, Arrays.asList( ip ), Charset.forName( "UTF-8" ), StandardOpenOption.APPEND, StandardOpenOption.CREATE );
            } catch ( IOException ex )
            {
                logger.log( Level.WARNING, "[BotFilter] Не могу записать прокси в файл", ex );
            }

        }
    }

    private void loadProxyFromFile(Path file)
    {
        try
        {
            File rfile = file.toFile();
            if ( !rfile.exists() )
            {
                rfile.createNewFile();
            }
            try ( Stream<String> lines = Files.lines( file, StandardCharsets.UTF_8 ) )
            {
                proxies.addAll( lines.collect( Collectors.toSet() ) );
            }
        } catch ( IOException ex )
        {
            logger.log( Level.WARNING, "[BotFilter] Не могу загрузить прокси из файлов", ex );
        }
    }

    private void downloadProxyFromSites()
    {
        ( downloadThread = new Thread( () ->
        {
            int downloaded = 0;
            for ( String site : Settings.IMP.PROXY.PROXY_SITES )
            {
                try
                {
                    URL downloadUrl = new URL( site );
                    URLConnection conn = downloadUrl.openConnection();
                    conn.setConnectTimeout( 35000 );
                    try ( BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) ) )
                    {
                        String inputLine;
                        while ( ( inputLine = in.readLine() ) != null && !Thread.interrupted() )
                        {
                            Matcher matcher = IPADDRESS_PATTERN.matcher( inputLine );
                            HashSet<String> dproxies = new HashSet<>();
                            while ( matcher.find() && !Thread.interrupted() )
                            {
                                String proxy = matcher.group( 0 );
                                if ( BotFilter.getInstance().getGeoIp().isAllowed( IPUtils.getAddress( matcher.group( 0 ) ) ) && !proxies.contains( proxy ) )
                                {
                                    dproxies.add( proxy );
                                    proxies.add( proxy );
                                    downloaded++;
                                }
                            }
                            Files.write( proxyFile, dproxies, Charset.forName( "UTF-8" ), StandardOpenOption.APPEND );

                        }
                    }
                } catch ( IOException ex )
                {
                    logger.log( Level.WARNING, "[BotFilter] Не могу загрузить прокси с сайта - " + site, ex );
                }
                if ( Thread.interrupted() )
                {
                    logger.log( Level.WARNING, "[BotFilter] Процесс скачивания прокси был прерван" );
                    return;
                }
            }
            logger.log( Level.INFO, "[BotFilter] Скачано {0} прокси", downloaded );
        }, "Proxy download thread" ) ).start();
    }

    public void close()
    {
        if ( downloadThread != null && !downloadThread.isInterrupted() )
        {
            downloadThread.interrupt();
        }
        proxies.clear();
    }
}
