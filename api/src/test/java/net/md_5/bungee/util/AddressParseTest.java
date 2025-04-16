package net.md_5.bungee.util;

import static org.junit.jupiter.api.Assertions.*;
import io.netty.channel.unix.DomainSocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.Util;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@RequiredArgsConstructor
public class AddressParseTest
{

    public static Stream<Arguments> data()
    {
        return Stream.of(
                Arguments.of( "127.0.0.1", "127.0.0.1", Util.DEFAULT_PORT ),
                Arguments.of( "127.0.0.1:1337", "127.0.0.1", 1337 ),
                Arguments.of( "[::1]", "0:0:0:0:0:0:0:1", Util.DEFAULT_PORT ),
                Arguments.of( "[0:0:0:0::1]", "0:0:0:0:0:0:0:1", Util.DEFAULT_PORT ),
                Arguments.of( "[0:0:0:0:0:0:0:1]", "0:0:0:0:0:0:0:1", Util.DEFAULT_PORT ),
                Arguments.of( "[::1]:1337", "0:0:0:0:0:0:0:1", 1337 ),
                Arguments.of( "[0:0:0:0::1]:1337", "0:0:0:0:0:0:0:1", 1337 ),
                Arguments.of( "[0:0:0:0:0:0:0:1]:1337", "0:0:0:0:0:0:0:1", 1337 ),
                Arguments.of( "unix:///var/run/bungee.sock", "/var/run/bungee.sock", -1 )
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void test(String line, String host, int port)
    {
        SocketAddress parsed = Util.getAddr( line );

        if ( parsed instanceof InetSocketAddress )
        {
            InetSocketAddress tcp = (InetSocketAddress) parsed;

            assertEquals( host, tcp.getHostString() );
            assertEquals( port, tcp.getPort() );
        } else if ( parsed instanceof DomainSocketAddress )
        {
            DomainSocketAddress unix = (DomainSocketAddress) parsed;

            assertEquals( host, unix.path() );
            assertEquals( -1, port );
        } else
        {
            throw new AssertionError( "Unknown socket " + parsed );
        }
    }
}
