package net.md_5.bungee.util;

import java.net.InetSocketAddress;
import org.junit.Assert;
import org.junit.Test;

public class AddressUtilTest
{

    @Test
    public void testScope()
    {
        InetSocketAddress addr = new InetSocketAddress( "0:0:0:0:0:0:0:1%0", 25577 );
        Assert.assertEquals( "0:0:0:0:0:0:0:1", AddressUtil.sanitizeAddress( addr ) );

        InetSocketAddress addr2 = new InetSocketAddress( "0:0:0:0:0:0:0:1", 25577 );
        Assert.assertEquals( "0:0:0:0:0:0:0:1", AddressUtil.sanitizeAddress( addr2 ) );
    }
}
