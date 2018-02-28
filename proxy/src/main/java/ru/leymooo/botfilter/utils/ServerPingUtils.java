package ru.leymooo.botfilter.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import ru.leymooo.botfilter.BotFilter;
import ru.leymooo.botfilter.config.Settings;

/**
 *
 * @author Leymooo
 */
public class ServerPingUtils
{

    private Cache<InetAddress, Boolean> pingList;
    private boolean enabled = Settings.IMP.SERVER_PING_CHECK.MODE != 2;

    public ServerPingUtils()
    {
        pingList = CacheBuilder.newBuilder()
                .concurrencyLevel( 2 )
                .initialCapacity( 40 )
                .expireAfterWrite( Settings.IMP.SERVER_PING_CHECK.CACHE_TIME, TimeUnit.SECONDS )
                .build();
    }

    public boolean needKickOrRemove(InetAddress address)
    {
        boolean present = pingList.getIfPresent( address ) == null;
        if ( !present ) //Убрираем из мапы есть уже есть в ней.
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
        return enabled && ( Settings.IMP.SERVER_PING_CHECK.MODE == 0 || BotFilter.getInstance().isUnderAttack() );
    }

}
