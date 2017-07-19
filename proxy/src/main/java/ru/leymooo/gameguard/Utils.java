package ru.leymooo.gameguard;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Leymooo
 */
public class Utils
{
    //Дропаем конекты, если за 10 минут их было больше трёх.
    public static Cache<String, Integer> connections = CacheBuilder.newBuilder()
            .initialCapacity( 40 )
            .expireAfterWrite( 10, TimeUnit.MINUTES )
            .build();
    
    public static boolean isManyChecks(String ip, boolean add)
    {
        Integer conns = connections.getIfPresent( ip );
        if ( conns != null && conns >= 3 )
        {
            Config.getConfig().getProxy().addProxyForce( ip );
            return true;
        }
        if ( add )
        {
            connections.put( ip, conns == null ? 1 : conns + 1 );
        }
        return false;
    }
}
