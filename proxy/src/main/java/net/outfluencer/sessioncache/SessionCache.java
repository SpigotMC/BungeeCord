package net.outfluencer.sessioncache;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.md_5.bungee.connection.LoginResult;

public class SessionCache
{

    private final ConcurrentHashMap<InetAddress, Map.Entry<LoginResult, Long>> sessions;
    private final long cacheTime;
    @Getter
    private final Timer timer = new Timer( "Session Cleaner" );

    public SessionCache(long cacheTime)
    {
        this.cacheTime = cacheTime;
        sessions = new ConcurrentHashMap<InetAddress, Map.Entry<LoginResult, Long>>( 100 );
        timer.scheduleAtFixedRate( new TimerTask()
        {
            @Override
            public void run()
            {
                long now = System.currentTimeMillis();
                sessions.entrySet().removeIf( (entry) -> now > entry.getValue().getValue() + cacheTime );
            }
        }, 0, TimeUnit.MINUTES.toMillis( 1 ) );
    }


    public void cacheSession(SocketAddress socketAddress, LoginResult loginResult)
    {
        if ( !( socketAddress instanceof InetSocketAddress ) )
        {
            return;
        }

        InetAddress address = ( (InetSocketAddress) socketAddress ).getAddress();
        sessions.put( address, new AbstractMap.SimpleImmutableEntry<LoginResult, Long>( loginResult, System.currentTimeMillis() ) );
    }

    public LoginResult getCachedResult(SocketAddress socketAddress)
    {
        if ( !( socketAddress instanceof InetSocketAddress ) )
        {
            return null;
        }

        InetAddress address = ( (InetSocketAddress) socketAddress ).getAddress();
        Map.Entry<LoginResult, Long> entry = sessions.get( address );
        if ( entry != null )
        {
            if ( System.currentTimeMillis() > entry.getValue() + cacheTime )
            {
                return null;
            } else
            {
                return entry.getKey();
            }
        }
        return null;
    }
}