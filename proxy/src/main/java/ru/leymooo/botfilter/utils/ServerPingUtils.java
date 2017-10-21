package ru.leymooo.botfilter.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import ru.leymooo.botfilter.Config;

/**
 *
 * @author Leymooo
 */
public class ServerPingUtils
{

    @Getter
    private static ServerPingUtils instance;
    private boolean onNormal, onPerm, onAttack;
    @Getter
    private Cache<InetAddress, Boolean> pingList;
    @Getter
    private String message;

    public ServerPingUtils(Configuration section)
    {
        instance = this;
        this.onNormal = section.getBoolean( "on-normal-mode" );
        this.onAttack = section.getBoolean( "on-bot-attack" );
        this.onPerm = section.getBoolean( "on-permanent-protection" );
        if ( !( onAttack || onPerm || onNormal ) )
        {
            return;
        }
        this.message = ChatColor.translateAlternateColorCodes( '&', section.getString( "kick-message" ) );
        pingList = CacheBuilder.newBuilder()
                .concurrencyLevel( 2 )
                .initialCapacity( 40 )
                .expireAfterWrite( section.getInt( "time" ), TimeUnit.SECONDS )
                .build();
    }

    public boolean needKickAndRemove(InetAddress address)
    {
        if ( pingList == null || !needCheck() )
        {
            return false;
        }
        boolean present = pingList.getIfPresent( address ) == null;
        if ( !present )
        {
            pingList.invalidate( address );
        }
        return present;
    }

    public void add(InetAddress address)
    {
        if ( pingList != null )
        {
            pingList.put( address, true );
        }
    }

    private boolean needCheck()
    {
        Config conf = Config.getConfig();
        return onNormal || ( onPerm && conf.isPermanent() ) || ( onAttack && conf.isUnderAttack() );
    }

}
