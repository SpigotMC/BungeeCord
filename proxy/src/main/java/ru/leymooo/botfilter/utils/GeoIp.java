package ru.leymooo.botfilter.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.botfilter.config.Settings;

/**
 * @author Leymooo
 */
public class GeoIp
{

    private static final Logger LOGGER = BungeeCord.getInstance().getLogger();

    private final HashSet<String> countries = new HashSet<>();
    @Getter
    private final Cache<InetAddress, String> cached;

    @Getter
    private final boolean enabled = Settings.IMP.GEO_IP.MODE != 2;

    private final boolean whiteList = Settings.IMP.GEO_IP.TYPE == 0;

    private DatabaseReader reader;

    public GeoIp(boolean startup)
    {
        if ( enabled )
        {
            countries.addAll( Settings.IMP.GEO_IP.ALLOWED_COUNTRIES );
            setupDataBase( startup );
            cached = CacheBuilder.newBuilder()
                    .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
                    .expireAfterAccess( 5, TimeUnit.MINUTES ).initialCapacity( 200 ).build();

        } else
        {
            cached = null;
        }
    }

    public boolean isAllowed(InetAddress address)
    {
        if ( address.isAnyLocalAddress() || address.isLoopbackAddress() )
        {
            return true;
        }
        String country = cached.getIfPresent( address );
        if ( country != null )
        {
            return whiteList ? countries.contains( country ) : /*blacklist*/ !countries.contains( country );
        }
        try
        {
            country = reader.country( address ).getCountry().getIsoCode();
        } catch ( IOException | GeoIp2Exception ex )
        {
            return false;
            //logger.log( Level.WARNING, "[BotFilter] Could not get country for " + address.getHostAddress() );
        }
        if ( country == null )
        {
            return false;
        }
        cached.put( address, country );
        return whiteList ? countries.contains( country ) : /*blacklist*/ !countries.contains( country );
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
            //file.delete();
            downloadDataBase( file );
        } else
        {
            try
            {
                reader = new DatabaseReader.Builder( file ).withCache( new CHMCache( 4096 * 4 ) ).build();
            } catch ( IOException ex )
            {
                LOGGER.log( Level.WARNING, "[BotFilter] На могу подключиться к GeoLite2 датабазе. Перекачиваю", ex );
                file.delete();
                setupDataBase( true );
            }
        }
    }

    private void downloadDataBase(final File out)
    {
        LOGGER.log( Level.INFO, "[BotFilter] Скачиваю GeoLite2 датабазу" );
        long start = System.currentTimeMillis();
        try
        {
            URL downloadUrl = new URL( Settings.IMP.GEO_IP.NEW_GEOIP_DOWNLOAD_URL.replace( "%license_key%", Settings.IMP.GEO_IP.MAXMIND_LICENSE_KEY ) );
            URLConnection conn = downloadUrl.openConnection();
            conn.setConnectTimeout( 5000 );
            conn.setReadTimeout( 10000 );
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
            if ( out.exists() )
            {
                setupDataBase( false );
            }
            LOGGER.log( Level.WARNING, "[BotFilter] Не могу скачать GeoLite2 датабазу", ex );
            return;
        }
        LOGGER.log( Level.INFO, "[BotFilter] GeoLite2 загружена ({0}мс)", System.currentTimeMillis() - start );
    }

    private void saveToFile(InputStream stream, File out) throws IOException
    {
        try ( FileOutputStream fis = new FileOutputStream( out, false ) )
        {
            byte[] buffer = new byte[ 2048 ];
            int count = 0;
            while ( ( count = stream.read( buffer, 0, 2048 ) ) != -1 )
            {
                if ( Thread.interrupted() )
                {
                    fis.close();
                    out.delete();
                    LOGGER.log( Level.WARNING, "[BotFilter] Не удалось скачать GeoLite2 датабазу. Удаляю недокачанный файл." );
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
        if ( cached != null )
        {
            cached.invalidateAll();
        }
    }

    public void tryClenUP()
    {
        if ( cached != null )
        {
            cached.cleanUp();
        }
    }

}
