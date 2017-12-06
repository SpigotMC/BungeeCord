
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import org.apache.commons.compress.utils.Lists;
import ru.leymooo.botfilter.utils.GeoIpUtils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author michael
 */
public class Test1
{

    public static void main(String[] args) throws Exception
    {
        BungeeCord bungee = new BungeeCord();
        ProxyServer.setInstance( bungee );
        File dataFolder = new File( "test" );
        dataFolder.mkdir();
        GeoIpUtils geo = new GeoIpUtils( dataFolder, Lists.newArrayList(), Lists.newArrayList() );
        Set<String> ips = Sets.newHashSet();

        URL url = new URL( "http://151.80.108.152/proxy.txt" );
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout( 3000 );
        try ( BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) ) )
        {
            String inputLine;
            while ( ( inputLine = in.readLine() ) != null )
            {
                ips.add( inputLine );
            }
        }

        System.out.println( "Proxies loaded: " + ips.size() );
        Long start = System.currentTimeMillis();
        System.out.println( "Start time is " + start );
        for ( String ip : ips )
        {
            geo.getCountryCode( InetAddress.getByName( ip ) );
        }
        System.out.println( "Time is " + ( System.currentTimeMillis() - start ) );
    }

}
