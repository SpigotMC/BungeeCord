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
}
