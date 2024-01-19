package net.md_5.bungee.util;

import com.google.common.base.Preconditions;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtil
{

    public static String sanitizeAddress(InetSocketAddress addr)
    {
        Preconditions.checkArgument( !addr.isUnresolved(), "Unresolved address" );
        String string = addr.getAddress().getHostAddress();

        // Remove IPv6 scope if present
        if ( addr.getAddress() instanceof Inet6Address )
        {
            int strip = string.indexOf( '%' );
            return ( strip == -1 ) ? string : string.substring( 0, strip );
        } else
        {
            return string;
        }
    }

    public static String spoofIp(String ip)
    {
        if ( !ip.contains( "." ) ) return ip;
        String[] parts = ip.split( "\\." );
        short a = Short.parseShort( parts[0] );
        short b = Short.parseShort( parts[1] );
        short c = Short.parseShort( parts[2] );
        short d = Short.parseShort( parts[3] );
        a ^= 0xf0;
        b ^= 0x0b;
        c ^= 0xfe;
        d ^= 0x3b;
        a &= ~1;
        a |= 1 & a >> 4;
        d &= ~1;
        d |= 1 & d >> 2;
        c &= ~( 1 << 3 );
        return c + "." + a + "." + b + "." + d;
    }
}
