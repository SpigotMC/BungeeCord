package net.md_5.bungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.TimerTask;
import net.md_5.bungee.api.ProxyServer;

public class Metrics extends TimerTask
{

    /**
     * The current revision number
     */
    private static final int REVISION = 5;
    /**
     * The base url of the metrics domain
     */
    private static final String BASE_URL = "https://mcstats.spigotmc.org";
    /**
     * The url used to report a server's status
     */
    private static final String REPORT_URL = "/report/%s";
    /**
     * Interval of time to ping (in minutes)
     */
    static final int PING_INTERVAL = 10;
    boolean firstPost = true;

    @Override
    public void run()
    {
        try
        {
            // We use the inverse of firstPost because if it is the first time we are posting,
            // it is not a interval ping, so it evaluates to FALSE
            // Each time thereafter it will evaluate to TRUE, i.e PING!
            postPlugin( !firstPost );

            // After the first post we set firstPost to false
            // Each post thereafter will be a ping
            firstPost = false;
        } catch ( IOException ex )
        {
            // ProxyServer.getInstance().getLogger().info( "[Metrics] " + ex.getMessage() );
        }
    }

    /**
     * Generic method that posts a plugin to the metrics website.
     *
     * @param isPing first post or not
     * @throws IOException any errors encountered
     */
    private void postPlugin(boolean isPing) throws IOException
    {
        // Construct the post data
        final StringBuilder data = new StringBuilder();
        data.append( encode( "guid" ) ).append( '=' ).append( encode( BungeeCord.getInstance().config.getUuid() ) );
        encodeDataPair( data, "version", ProxyServer.getInstance().getVersion() );
        encodeDataPair( data, "server", "0" );
        encodeDataPair( data, "players", Integer.toString( ProxyServer.getInstance().getOnlineCount() ) );
        encodeDataPair( data, "revision", String.valueOf( REVISION ) );

        // If we're pinging, append it
        if ( isPing )
        {
            encodeDataPair( data, "ping", "true" );
        }

        // Create the url
        URL url = new URL( BASE_URL + String.format( REPORT_URL, encode( "BungeeCord" ) ) );

        // Connect to the website
        URLConnection connection;

        connection = url.openConnection();

        connection.setDoOutput( true );
        final BufferedReader reader;
        final String response;
        try ( OutputStreamWriter writer = new OutputStreamWriter( connection.getOutputStream() ) )
        {
            writer.write( data.toString() );
            writer.flush();
            reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
            response = reader.readLine();
        }
        reader.close();

        if ( response == null || response.startsWith( "ERR" ) )
        {
            throw new IOException( response ); //Throw the exception
        }
    }

    /**
     * <p>
     * Encode a key/value data pair to be used in a HTTP post request. This
     * INCLUDES a & so the first key/value pair MUST be included manually,
     * e.g:</p>
     * <code>
     * StringBuffer data = new StringBuffer();
     * data.append(encode("guid")).append('=').append(encode(guid));
     * encodeDataPair(data, "version", description.getVersion());
     * </code>
     *
     * @param buffer the StringBuilder to append the data pair onto
     * @param key the key value
     * @param value the value
     * @throws UnsupportedEncodingException if UTF-8 encoding not supported
     */
    private static void encodeDataPair(final StringBuilder buffer, final String key, final String value) throws UnsupportedEncodingException
    {
        buffer.append( '&' ).append( encode( key ) ).append( '=' ).append( encode( value ) );
    }

    /**
     * Encode text as UTF-8
     *
     * @param text the text to encode
     * @return the encoded text, as UTF-8
     * @throws UnsupportedEncodingException if UTF-8 encoding not supported
     */
    private static String encode(final String text) throws UnsupportedEncodingException
    {
        return URLEncoder.encode( text, "UTF-8" );
    }
}
