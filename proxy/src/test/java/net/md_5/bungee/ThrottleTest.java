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
        ConnectionThrottle throttle = new ConnectionThrottle( 10 );
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

        Thread.sleep( 50 );
        Assert.assertFalse( "Address should not be throttled", throttle.throttle( address ) );
    }
}
