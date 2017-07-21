package ru.leymooo.gameguard.utils;

import com.maxmind.geoip.LookupService;
import static com.maxmind.geoip.LookupService.GEOIP_MEMORY_CACHE;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import net.md_5.bungee.BungeeCord;

/**
 *
 * @author AuthMeReloaded Team
 */
public class GeoIpUtils
{

    private static final String LICENSE
            = "[LICENSE] This product uses data from the GeoLite API created by MaxMind, available at http://www.maxmind.com";
    private static final String GEOIP_URL
            = "http://geolite.maxmind.com/download/geoip/database/GeoLiteCountry/GeoIP.dat.gz";

    private final HashSet<String> countryAuto;
    private final HashSet<String> countryPermanent;

    private LookupService lookupService;
    private Thread downloadTask;

    private final File dataFile;

    public GeoIpUtils(File dataFolder, List<String> auto, List<String> permanent)
    {
        this.dataFile = new File( dataFolder, "GeoIP.dat" );
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
        if ( lookupService != null )
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
                    lookupService = new LookupService( dataFile, GEOIP_MEMORY_CACHE );
                    BungeeCord.getInstance().getLogger().info( LICENSE );
                    return true;
                } catch ( IOException e )
                {
                    BungeeCord.getInstance().getLogger().log( Level.WARNING, "Failed to load GeoLiteAPI database", e );
                    return false;
                }
            } else
            {
                dataFile.delete();
            }
        }
        // Ok, let's try to download the data file!
        downloadTask = createDownloadTask();
        downloadTask.start();
        return false;
    }

    /**
     * Create a thread which will attempt to download new data from the GeoLite
     * website.
     *
     * @return the generated download thread
     */
    private Thread createDownloadTask()
    {
        return new Thread( () ->
        {
            try
            {
                URL downloadUrl = new URL( GEOIP_URL );
                URLConnection conn = downloadUrl.openConnection();
                conn.setConnectTimeout( 10000 );
                conn.connect();
                InputStream input = conn.getInputStream();
                if ( conn.getURL().toString().endsWith( ".gz" ) )
                {
                    input = new GZIPInputStream( input );
                }
                OutputStream output = new FileOutputStream( dataFile );
                byte[] buffer = new byte[ 2048 ];
                int length = input.read( buffer );
                while ( length >= 0 )
                {
                    output.write( buffer, 0, length );
                    length = input.read( buffer );
                }
                output.close();
                input.close();
            } catch ( IOException e )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not download GeoLiteAPI database", e );
            }
        } );
    }

    /**
     * Get the country code of the given IP address.
     *
     * @param ip textual IP address to lookup.
     *
     * @return two-character ISO 3166-1 alpha code for the country.
     */
    public String getCountryCode(String ip)
    {
        if ( isDataAvailable() )
        {
            return lookupService.getCountry( ip ).getCode();
        }
        return "--";
    }

    public boolean isAllowed(String code, boolean permanent)
    {
        return ( countryAuto.contains( code ) || code.equals( "--" ) ) || ( permanent && countryPermanent.contains( code ) );
    }

}
