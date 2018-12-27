package net.md_5.bungee;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Assert;
import org.junit.Test;

public class ThrottleTest
{

    @Test
    public void testThrottle() throws InterruptedException, UnknownHostException
    {
        ConnectionThrottle throttle = new ConnectionThrottle( 10, 3 );
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
        Thread.sleep( 50 );
        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) );
    }
}
