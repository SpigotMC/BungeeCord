package net.md_5.bungee;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class ConnectionThrottle
{

    private final LoadingCache<InetAddress, Integer> throttle;
    private final int throttleLimit;

    public ConnectionThrottle(int throttleTime, int throttleLimit)
    {
        this.throttle = CacheBuilder.newBuilder()
                .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
                .initialCapacity( 100 )
                .expireAfterWrite( throttleTime, TimeUnit.MILLISECONDS )
                .build( new CacheLoader<InetAddress, Integer>()
                {
                    @Override
                    public Integer load(InetAddress key) throws Exception
                    {
                        return 0;
                    }
                } );
        this.throttleLimit = throttleLimit;
    }

    public void unthrottle(InetAddress address)
    {
        int throttleCount = throttle.getUnchecked( address ) - 1;
        throttle.put( address, throttleCount );
    }

    public boolean throttle(InetAddress address)
    {
        int throttleCount = throttle.getUnchecked( address ) + 1;
        throttle.put( address, throttleCount );

        return throttleCount > throttleLimit;
    }
}
