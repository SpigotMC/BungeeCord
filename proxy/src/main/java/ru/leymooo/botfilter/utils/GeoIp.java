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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javolution.util.FastMap;
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
    private final FastMap<InetAddress, String> cached = new FastMap<>();
    private final Logger logger = BungeeCord.getInstance().getLogger();

    @Getter
    private boolean enabled = Settings.IMP.GEO_IP.MODE != 2;
    @Getter
    private boolean downloading = false;

    private DatabaseReader reader;

    public GeoIp()
    {
        if ( enabled )
        {
            countries.addAll( Settings.IMP.GEO_IP.ALLOWED_COUNTRIES );
            setupDataBase( false );
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

    private void setupDataBase(boolean isReload)
    {
        File file = new File( "BotFilter", "GeoIP.mmdb" );
        if ( !file.exists() || ( !isReload && ( System.currentTimeMillis() - file.lastModified() ) > TimeUnit.DAYS.toMillis( 14 ) ) )
        {
            file.delete();
            downloadDataBase( file );
        } else
        {
            try
            {
                reader = new DatabaseReader.Builder( file ).withCache( new CHMCache( 4096 * 4 ) ).build();
                logger.info( "[LICENSE] This product uses data from the GeoLite2 API created by MaxMind, available at http://www.maxmind.com" );
            } catch ( IOException ex )
            {
                logger.log( Level.WARNING, "[BotFilter] Could not setup database", ex );
                file.delete();
                setupDataBase( false );
            }
        }
    }

    private void downloadDataBase(final File out)
    {
        if ( downloading )
        {
            return;
        }
        downloading = true;
        logger.log( Level.INFO, "[BotFilter] Downloading GeoLite2 DataBase" );
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
            setupDataBase( false );
        } catch ( Exception ex )
        {
            logger.log( Level.WARNING, "[BotFilter] Could not download database", ex );
            return;
        } finally
        {
            downloading = false;
        }
        logger.log( Level.INFO, "[BotFilter] GeoLite2 DataBase downloaded({0})", System.currentTimeMillis() - start );
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
                    logger.log( Level.WARNING, "[BotFilter] GeoIp download was failed. Removing file" );
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
