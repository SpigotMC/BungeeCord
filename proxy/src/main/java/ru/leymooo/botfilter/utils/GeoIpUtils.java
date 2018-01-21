package ru.leymooo.botfilter.utils;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import net.md_5.bungee.BungeeCord;

/**
 *
 * @author AuthMeReloaded Team, Leymooo
 */
//1300000 айпишек отрабатывает за 6 секунд.
public class GeoIpUtils
{

    private static final String LICENSE
            = "[LICENSE] This product uses data from the GeoLite API created by MaxMind, available at http://www.maxmind.com";
    private static final String GEOIP_URL
            = "http://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.tar.gz";

    private final HashSet<String> countryAuto;
    private final HashSet<String> countryPermanent;

    private DatabaseReader reader;
    private Thread downloadTask;

    private final File dataFile;

    public GeoIpUtils(File dataFolder, List<String> auto, List<String> permanent)
    {
        this.dataFile = new File( dataFolder, "GeoIP.mmdb" );
        this.countryAuto = new HashSet<>( auto );
        this.countryPermanent = new HashSet<>( permanent );
        // Fires download of recent data or the initialization of the look up service
        isDataAvailable();
    }

    /**
     * Download (if absent or old) the GeoIpLite data file and then try to load
     * it.
     *
     * @return True if the data is available, false otherwise.
     */
    public synchronized boolean isDataAvailable()
    {
        if ( downloadTask != null && downloadTask.isAlive() )
        {
            return false;
        }
        if ( reader != null )
        {
            return true;
        }

        if ( dataFile.exists() )
        {
            boolean dataIsOld = ( System.currentTimeMillis() - dataFile.lastModified() ) > TimeUnit.DAYS.toMillis( 7 );
            if ( !dataIsOld )
            {
                try
                {
                    reader = new DatabaseReader.Builder( dataFile ).withCache( new CHMCache( 4096 * 4 ) ).build();
                } catch ( IOException ex )
                {
                    BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not setup GeoLiteAPI database", ex );
                    return false;
                }
                BungeeCord.getInstance().getLogger().info( LICENSE );
                return true;
            } else
            {
                dataFile.delete();
            }
        }
        // Ok, let's try to download the data file!
        downloadTask = new Thread( createDownloadTask() );
        downloadTask.start();
        return false;
    }

    /**
     * Create a thread which will attempt to download new data from the GeoLite
     * website.
     *
     * @return the generated download thread
     */
    public Runnable createDownloadTask()
    {
        return () ->
        {
            try
            {
                URL downloadUrl = new URL( GEOIP_URL );
                URLConnection conn = downloadUrl.openConnection();
                conn.setConnectTimeout( 10000 );
                try ( InputStream input = conn.getInputStream() )
                {
                    extractTarGZ( input );
                }
                reader = new DatabaseReader.Builder( dataFile ).withCache( new CHMCache( 4096 * 4 ) ).build();
            } catch ( IOException e )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not download GeoLiteAPI database", e );
            }
        };
    }

    /**
     * Get the country code of the given IP address.
     *
     * @param ip textual IP address to lookup.
     *
     * @return two-character ISO 3166-1 alpha code for the country.
     */
    public String getCountryCode(InetAddress ip)
    {
        if ( reader != null && !isLocal( ip ) )
        {
            CountryResponse response = null;
            try
            {
                response = reader.country( ip );
            } catch ( IOException | GeoIp2Exception | NullPointerException ex )
            {
            }
            return response == null ? "--" : response.getCountry().getIsoCode();
        }
        return "N/A";
    }

    private boolean isLocal(InetAddress addr)
    {
        return addr.isAnyLocalAddress() || addr.isLoopbackAddress();
    }

    public boolean isAllowed(String code, boolean permanent)
    {
        return ( "N/A".equals( code ) ) || ( countryAuto.contains( code ) ) || ( permanent && countryPermanent.contains( code ) );
    }

    public void extractTarGZ(InputStream in) throws IOException
    {

        try ( GZIPInputStream gzipIn = new GZIPInputStream( in ); TarInputStream tarIn = new TarInputStream( gzipIn ) )
        {
            TarEntry entry;

            while ( ( entry = (TarEntry) tarIn.getNextEntry() ) != null )
            {
                if ( entry.getName().endsWith( "mmdb" ) )
                {
                    int count;
                    byte data[] = new byte[ 4096 ];
                    try ( FileOutputStream fos = new FileOutputStream( dataFile, false );
                            BufferedOutputStream dest = new BufferedOutputStream( fos, 4096 ) )
                    {
                        while ( ( count = tarIn.read( data, 0, 4096 ) ) != -1 )
                        {
                            dest.write( data, 0, count );
                        }
                    }
                }
            }
        }
    }

    public boolean isAvailable()
    {
        return this.reader != null;
    }

    public void close()
    {
        if ( this.reader != null )
        {
            try
            {
                reader.close();
            } catch ( IOException ex )
            {
                //Думаю можно и проигнорить
            }
        }
    }
}
