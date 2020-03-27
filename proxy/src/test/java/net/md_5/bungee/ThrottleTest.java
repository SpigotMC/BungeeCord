package net.md_5.bungee;

import com.github.benmanes.caffeine.cache.Ticker;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ThrottleTest
{

    private class FixedTicker implements Ticker
    {

        private long value;

        @Override
        public long read()
        {
            return value;
        }
    }

    @Test
    public void testThrottle() throws InterruptedException, UnknownHostException
    {
        FixedTicker ticker = new FixedTicker();
        ConnectionThrottle throttle = new ConnectionThrottle( ticker, 10, 3 );
        InetSocketAddress address;

        try
        {
            address = new InetSocketAddress( InetAddress.getLocalHost(), 0 );
        } catch ( UnknownHostException ex )
        {
            address = new InetSocketAddress( InetAddress.getByName( null ), 0 );
        }

        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) ); // 1
        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) ); // 2
        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) ); // 3
        Assert.assertTrue( "Address should be throttled", throttle.throttle( address ) ); // The 3rd one must be throttled, but also increased the count to 4

        throttle.unthrottle( address ); // We are back at 3, next attempt will make it 4 and throttle
        throttle.unthrottle( address ); // Now we are at 2, will not be throttled
        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) ); // 3
        Assert.assertTrue( "Address should be throttled", throttle.throttle( address ) ); // 4

        // Now test expiration
        ticker.value += TimeUnit.MILLISECONDS.toNanos( 50 );
        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) );
    }
}
