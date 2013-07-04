package net.md_5.bungee.http;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpClient
{

    private final EventLoopGroup eventLoop;

    public void get(String url)
    {
        URI uri = null;
        try
        {
            uri = new URI( url );
        } catch ( URISyntaxException ex )
        {
            throw new IllegalArgumentException( "Could not parse url " + url, ex );
        }

        Preconditions.checkNotNull( uri.getScheme(), "scheme" );
        Preconditions.checkNotNull( uri.getHost(), "host" );
        boolean ssl = false;
        int port = uri.getPort();
        if ( port == -1 )
        {
            switch ( uri.getScheme() )
            {
                case "http":
                    port = 80;
                    break;
                case "https":
                    port = 443;
                    break;
                default:
                    throw new IllegalArgumentException( "Unknown scheme " + uri.getScheme() );
            }
        }

        new Bootstrap().channel( NioSocketChannel.class ).group( eventLoop ).handler( new HttpInitializer( url, port, ssl ) ).remoteAddress( uri.getHost(), port ).connect();

        HttpRequest request = new DefaultHttpRequest( HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath() );
        request.headers().set( HttpHeaders.Names.HOST, uri.getHost() );
    }
}
