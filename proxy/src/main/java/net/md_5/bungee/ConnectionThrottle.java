package net.md_5.bungee;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class ConnectionThrottle
{

    private final Cache<InetAddress, Boolean> throttle;

    public ConnectionThrottle(int throttleTime)
    {
        this.throttle = CacheBuilder.newBuilder()
                .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
                .initialCapacity( 100 )
                .expireAfterWrite( throttleTime, TimeUnit.MILLISECONDS )
                .build();
    }

    public boolean throttle(InetAddress address)
    {
        boolean isThrottled = throttle.getIfPresent( address ) != null;
        throttle.put( address, true );

        return isThrottled;
    }
}
