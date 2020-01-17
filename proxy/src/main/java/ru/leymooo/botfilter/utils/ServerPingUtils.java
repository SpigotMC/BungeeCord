package ru.leymooo.botfilter.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import ru.leymooo.botfilter.BotFilter;
import ru.leymooo.botfilter.config.Settings;

/**
 * @author Leymooo
 */
public class ServerPingUtils
{

    private Cache<InetAddress, Boolean> pingList;
    private boolean enabled = Settings.IMP.SERVER_PING_CHECK.MODE != 2;

    private final BotFilter botFilter;

    public ServerPingUtils(BotFilter botFilter)
    {
        this.botFilter = botFilter;
        pingList = CacheBuilder.newBuilder()
                .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
                .initialCapacity( 100 )
                .expireAfterWrite( Settings.IMP.SERVER_PING_CHECK.CACHE_TIME, TimeUnit.SECONDS )
                .build();
    }

    public boolean needKickOrRemove(InetAddress address)
    {
        boolean present = pingList.getIfPresent( address ) == null;
        if ( !present ) //Убрираем из мапы если есть уже есть в ней.
        {
            pingList.invalidate( address );
        }
        return present;
    }

    public void add(InetAddress address)
    {
        if ( enabled )
        {
            pingList.put( address, true );
        }
    }

    public boolean needCheck()
    {
        return enabled && ( Settings.IMP.SERVER_PING_CHECK.MODE == 0 || botFilter.isUnderAttack() );
    }

    public void clear()
    {
        pingList.invalidateAll();
    }

    public void cleanUP()
    {
        pingList.cleanUp();
    }
}
