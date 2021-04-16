package ru.leymooo.botfilter.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.config.Settings;

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
        if ( Settings.IMP.PROTECTION.CHECK_LOCALHOST != 1 && address.isLoopbackAddress() )
        {
            return;
        }
        Integer numOfCon = connections.getIfPresent( address );
        if ( numOfCon != null && numOfCon >= 3 )
        {
            return;
        }

        Integer newValue = numOfCon == null ? 1 : numOfCon + 1;
        if ( newValue == 3 )
        {
            FailedUtils.addIpToQueue( address.getHostAddress(), PacketUtils.KickType.MANYCHECKS );
        }
        connections.put( address, newValue );
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
