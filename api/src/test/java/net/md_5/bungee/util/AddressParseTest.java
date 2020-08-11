package net.md_5.bungee.util;

import io.netty.channel.unix.DomainSocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.Util;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RequiredArgsConstructor
@RunWith(Parameterized.class)
public class AddressParseTest
{

    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList( new Object[][]
        {
            {
                "127.0.0.1", "127.0.0.1", Util.DEFAULT_PORT
            },
            {
                "127.0.0.1:1337", "127.0.0.1", 1337
            },
            {
                "[::1]", "0:0:0:0:0:0:0:1", Util.DEFAULT_PORT
            },
            {
                "[0:0:0:0::1]", "0:0:0:0:0:0:0:1", Util.DEFAULT_PORT
            },
            {
                "[0:0:0:0:0:0:0:1]", "0:0:0:0:0:0:0:1", Util.DEFAULT_PORT
            },
            {
                "[::1]:1337", "0:0:0:0:0:0:0:1", 1337
            },
            {
                "[0:0:0:0::1]:1337", "0:0:0:0:0:0:0:1", 1337
            },
            {
                "[0:0:0:0:0:0:0:1]:1337", "0:0:0:0:0:0:0:1", 1337
            },
            {
                "unix:///var/run/bungee.sock", "/var/run/bungee.sock", -1
            }
        } );
    }
    private final String line;
    private final String host;
    private final int port;

    @Test
    public void test()
    {
        SocketAddress parsed = Util.getAddr( line );

        if ( parsed instanceof InetSocketAddress )
        {
            InetSocketAddress tcp = (InetSocketAddress) parsed;

            Assert.assertEquals( host, tcp.getHostString() );
            Assert.assertEquals( port, tcp.getPort() );
        } else if ( parsed instanceof DomainSocketAddress )
        {
            DomainSocketAddress unix = (DomainSocketAddress) parsed;

            Assert.assertEquals( host, unix.path() );
            Assert.assertEquals( -1, port );
        } else
        {
            throw new AssertionError( "Unknown socket " + parsed );
        }
    }
}
