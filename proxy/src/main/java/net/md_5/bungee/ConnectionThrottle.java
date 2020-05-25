package net.md_5.bungee;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Ticker;
import com.google.common.annotations.VisibleForTesting;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionThrottle
{

    private final LoadingCache<InetAddress, AtomicInteger> throttle;
    private final int throttleLimit;

    public ConnectionThrottle(int throttleTime, int throttleLimit)
    {
        this( Ticker.systemTicker(), throttleTime, throttleLimit );
    }

    @VisibleForTesting
    ConnectionThrottle(Ticker ticker, int throttleTime, int throttleLimit)
    {
        this.throttle = Caffeine.newBuilder()
                .ticker( ticker )
                //.concurrencyLevel( Runtime.getRuntime().availableProcessors() )
                .initialCapacity( 100 )
                .expireAfterWrite( throttleTime, TimeUnit.MILLISECONDS )
                .build( key -> new AtomicInteger() );
        this.throttleLimit = throttleLimit;
    }

    public void unthrottle(SocketAddress socketAddress)
    {
        if ( !( socketAddress instanceof InetSocketAddress ) )
        {
            return;
        }

        InetAddress address = ( (InetSocketAddress) socketAddress ).getAddress();

        AtomicInteger throttleCount = throttle.get( address );
        if ( throttleCount != null )
        {
            throttleCount.decrementAndGet();
        }
    }

    public boolean throttle(SocketAddress socketAddress)
    {
        if ( !( socketAddress instanceof InetSocketAddress ) )
        {
            return false;
        }

        InetAddress address = ( (InetSocketAddress) socketAddress ).getAddress();
        int throttleCount = throttle.get( address ).incrementAndGet();

        return throttleCount > throttleLimit;
    }

    public void cleanUP()
    {
        throttle.cleanUp();
    }
}
