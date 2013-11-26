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
        ConnectionThrottle throttle = new ConnectionThrottle( 5 );
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

        throttle.unthrottle( address );
        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) );

        Thread.sleep( 15 );
        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) );
    }
}
