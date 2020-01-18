package net.md_5.bungee;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class ConnectionThrottle
{

    private final LoadingCache<InetAddress, Integer> throttle;
    private final int throttleLimit;

    public ConnectionThrottle(int throttleTime, int throttleLimit)
    {
        this( Ticker.systemTicker(), throttleTime, throttleLimit );
    }

    @VisibleForTesting
    ConnectionThrottle(Ticker ticker, int throttleTime, int throttleLimit)
    {
        this.throttle = CacheBuilder.newBuilder()
                .ticker( ticker )
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

    public void unthrottle(SocketAddress socketAddress)
    {
        if ( !( socketAddress instanceof InetSocketAddress ) )
        {
            return;
        }

        InetAddress address = ( (InetSocketAddress) socketAddress ).getAddress();
        int throttleCount = throttle.getUnchecked( address ) - 1;
        throttle.put( address, throttleCount );
    }

    public boolean throttle(SocketAddress socketAddress)
    {
        if ( !( socketAddress instanceof InetSocketAddress ) )
        {
            return false;
        }

        InetAddress address = ( (InetSocketAddress) socketAddress ).getAddress();
        int throttleCount = throttle.getUnchecked( address ) + 1;
        throttle.put( address, throttleCount );

        return throttleCount > throttleLimit;
    }
}
