package ru.leymooo.botfilter.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Leymooo
 */
public class ManyChecksUtils
{

    private static Cache<InetAddress, Integer> connections = CacheBuilder.newBuilder()
            .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
            .initialCapacity( 100 )
            .expireAfterWrite( 10, TimeUnit.MINUTES )
            .build();

    public static void IncreaseOrAdd(InetAddress address)
    {
        Integer numOfCon = connections.getIfPresent( address );
        if ( numOfCon != null && numOfCon >= 3 )
        {
            return;
        }
        connections.put( address, numOfCon == null ? 1 : numOfCon + 1 );
    }

    public static boolean isManyChecks(InetAddress address)
    {
        Integer numOfCon = connections.getIfPresent( address );
        return numOfCon != null && numOfCon >= 3;
    }

    public static void clear()
    {
        connections.invalidateAll();
    }

    public static void cleanUP()
    {
        connections.cleanUp();
    }
}
