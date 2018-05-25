package ru.leymooo.botfilter.utils;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.botfilter.config.Settings;

/**
 *
 * @author Leymooo
 */
public class GeoIp
{

    private static String NA = "N/A";

    private final HashSet<String> countries = new HashSet<>();
    @Getter
    private final Map<InetAddress, String> cached = new ConcurrentHashMap<>();
    @Getter
    private boolean enabled = Settings.IMP.GEO_IP.MODE != 2;

    private final Logger logger = BungeeCord.getInstance().getLogger();

    private DatabaseReader reader;

    public GeoIp(boolean startup)
    {
        if ( enabled )
        {
            countries.addAll( Settings.IMP.GEO_IP.ALLOWED_COUNTRIES );
            setupDataBase( startup );
        }
    }

    public boolean isAllowed(InetAddress address)
    {
        if ( !enabled || reader == null || address.isAnyLocalAddress() || address.isLoopbackAddress() )
        {
            return true;
        }
        String country;
        if ( !( country = cached.getOrDefault( address, NA ) ).equals( NA ) )
        {
            return countries.contains( country );
        }
        try
        {
            country = reader.country( address ).getCountry().getIsoCode();
        } catch ( IOException | GeoIp2Exception ex )
        {
            return false;
            //logger.log( Level.WARNING, "[BotFilter] Could not get country for " + address.getHostAddress() );
        }
        cached.put( address, country );
        return countries.contains( country );
    }

    public boolean isAvailable()
    {
        return reader != null;
    }

    private void setupDataBase(boolean startup)
    {
        File file = new File( "BotFilter", "GeoIP.mmdb" );
        if ( !file.exists() || ( startup && ( System.currentTimeMillis() - file.lastModified() ) > TimeUnit.DAYS.toMillis( 14 ) ) )
        {
            file.delete();
            downloadDataBase( file );
        } else
        {
            try
            {
                reader = new DatabaseReader.Builder( file ).withCache( new CHMCache( 4096 * 4 ) ).build();
            } catch ( IOException ex )
            {
                logger.log( Level.WARNING, "[BotFilter] На могу подключиться к GeoLite2 датабазе. Перекачиваю", ex );
                file.delete();
                setupDataBase( true );
            }
        }
    }

    private void downloadDataBase(final File out)
    {
        logger.log( Level.INFO, "[BotFilter] Скачиваю GeoLite2 датабазу" );
        long start = System.currentTimeMillis();
        try
        {
            URL downloadUrl = new URL( Settings.IMP.GEO_IP.GEOIP_DOWNLOAD_URL );
            URLConnection conn = downloadUrl.openConnection();
            conn.setConnectTimeout( 35000 );
            try ( InputStream input = conn.getInputStream() )
            {
                if ( downloadUrl.getFile().endsWith( ".mmdb" ) )
                {
                    saveToFile( input, out );
                } else if ( downloadUrl.getFile().endsWith( "tar.gz" ) )
                {
                    try ( GZIPInputStream gzipIn = new GZIPInputStream( input ); TarInputStream tarIn = new TarInputStream( gzipIn ) )
                    {
                        TarEntry entry;
                        while ( ( entry = (TarEntry) tarIn.getNextEntry() ) != null )
                        {
                            if ( entry.getName().endsWith( "mmdb" ) )
                            {
                                saveToFile( tarIn, out );
                            }
                        }
                    }
                } else
                {
                    throw new IOException( "File type is not supported " );
                }
            }
            setupDataBase( true );
        } catch ( Exception ex )
        {
            logger.log( Level.WARNING, "[BotFilter] Не могу скачать GeoLite2 датабазу", ex );
            return;
        }
        logger.log( Level.INFO, "[BotFilter] GeoLite2 загружена ({0}мс)", System.currentTimeMillis() - start );
    }

    private void saveToFile(InputStream stream, File out) throws IOException
    {
        try ( FileOutputStream fis = new FileOutputStream( out ) )
        {
            byte[] buffer = new byte[ 2048 ];
            int count = 0;
            while ( ( count = stream.read( buffer, 0, 2048 ) ) != -1 )
            {
                if ( Thread.interrupted() )
                {
                    fis.close();
                    out.delete();
                    logger.log( Level.WARNING, "[BotFilter] Не удалось скачать GeoLite2 датабазу. Удаляю недокачанный файл." );
                    return;
                }
                fis.write( buffer, 0, count );
            }
        }
    }

    public void close()
    {
        if ( reader != null )
        {
            try
            {
                reader.close();
            } catch ( IOException ignore )
            {
            }
        }
        cached.clear();
    }

}
