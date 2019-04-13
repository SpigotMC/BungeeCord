package net.md_5.bungee;

import com.google.common.base.Ticker;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ThrottleTest
{

    private class FixedTicker extends Ticker
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
        InetAddress address;

        try
        {
            address = InetAddress.getLocalHost();
        } catch ( UnknownHostException ex )
        {
            address = InetAddress.getByName( null );
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
