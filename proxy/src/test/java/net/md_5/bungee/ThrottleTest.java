package net.md_5.bungee;

import com.google.common.testing.FakeTicker;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ThrottleTest
{

    @Test
    public void testThrottle() throws InterruptedException, UnknownHostException
    {
        FakeTicker ticker = new FakeTicker();
        ConnectionThrottle throttle = new ConnectionThrottle( 10, ticker );
        InetAddress address;

        try
        {
            address = InetAddress.getLocalHost();
        } catch ( UnknownHostException ex )
        {
            address = InetAddress.getByName( null );
        }

        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) );
        Assert.assertTrue( "Address should be throttled", throttle.throttle( address ) );
        
        ticker.advance( 6, TimeUnit.MILLISECONDS );
        Assert.assertTrue( "Address should be throttled", throttle.throttle( address ) );
        
        ticker.advance( 6, TimeUnit.MILLISECONDS );
        Assert.assertTrue( "Address should be throttled again", throttle.throttle( address ) );

        ticker.advance( 11, TimeUnit.MILLISECONDS );
        Assert.assertFalse( "Address should not be throttled anymore", throttle.throttle( address ) );
    }
}
