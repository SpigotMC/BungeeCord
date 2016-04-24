package net.md_5.bungee;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class ConnectionThrottle
{

    private final int throttleTime;
    private final Cache<InetAddress, Long> throttle;

    public ConnectionThrottle(int throttleTime)
    {
        this.throttleTime = throttleTime;
        this.throttle = CacheBuilder.newBuilder()
                .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
                .initialCapacity( 100 )
                .expireAfterAccess( throttleTime, TimeUnit.MILLISECONDS )
                .build();
    }

    public boolean throttle(InetAddress address)
    {
        Long value = throttle.getIfPresent( address );
        long currentTime = System.currentTimeMillis();

        throttle.put( address, currentTime );
        return value != null && currentTime - value < throttleTime;
    }
}
