package net.md_5.bungee.netty;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Leymooo
 */
public class ConnectionDropper
{

    private static Cache<String, Integer> connections = CacheBuilder.newBuilder()
            .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
            .initialCapacity( 100 )
            .expireAfterWrite( 1500, TimeUnit.MILLISECONDS )
            .build();

    public static boolean needDrop(SocketAddress address)
    {
        if ( address == null || !( address instanceof InetSocketAddress ) )
        {
            return true;
        }
        
        InetSocketAddress iadress = (InetSocketAddress) address;
        if ( iadress.getAddress() == null )
        {
            return true;
        }
        
        boolean needDrop = false;
        String ip = iadress.getAddress().getHostAddress();

        Integer conns = connections.getIfPresent( ip );
        if ( conns != null && conns >= 4 )
        {
            needDrop = true;
        }
        connections.put( ip, conns == null ? 1 : conns + 1 );
        return conns != null && connections.size() > 350 || needDrop == true;
    }
}
