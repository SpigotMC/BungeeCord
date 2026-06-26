package net.md_5.bungee.util;

import static org.junit.jupiter.api.Assertions.*;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;

public class AddressUtilTest
{

    @Test
    public void testScope()
    {
        InetSocketAddress addr = new InetSocketAddress( "0:0:0:0:0:0:0:1%0", 25577 );
        assertEquals( "0:0:0:0:0:0:0:1", AddressUtil.sanitizeAddress( addr ) );

        InetSocketAddress addr2 = new InetSocketAddress( "0:0:0:0:0:0:0:1", 25577 );
        assertEquals( "0:0:0:0:0:0:0:1", AddressUtil.sanitizeAddress( addr2 ) );
    }

    @Test
    public void testUnresolvedAddress()
    {
        InetSocketAddress unresolved = InetSocketAddress.createUnresolved( "localhost", 8080 );
        assertThrows( IllegalArgumentException.class, () -> AddressUtil.sanitizeAddress( unresolved ) );
    }

    @Test
    public void testIPv4Address()
    {
        InetSocketAddress addr = new InetSocketAddress( "127.0.0.1", 8080 );
        assertEquals( "127.0.0.1", AddressUtil.sanitizeAddress( addr ) );
    }
}
