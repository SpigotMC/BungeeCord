package net.md_5.bungee.http;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import java.net.URI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpClient
{

    private final EventLoopGroup eventLoop;

    public static void main(String[] args)
    {
        new HttpClient( new NioEventLoopGroup( 1 ) ).get( "https://session.minecraft.net/" );
    }

    public void get(String url)
    {
        final URI uri = URI.create( url );

        Preconditions.checkNotNull( uri.getScheme(), "scheme" );
        Preconditions.checkNotNull( uri.getHost(), "host" );
        boolean ssl = uri.getScheme().equals( "https" );
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

        ChannelFutureListener future = new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( future.isSuccess() )
                {
                    HttpRequest request = new DefaultHttpRequest( HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath() );
                    request.headers().set( HttpHeaders.Names.HOST, uri.getHost() );

                    future.channel().write( request );
                } else
                {
                }
            }
        };

        new Bootstrap().channel( NioSocketChannel.class ).group( eventLoop ).handler( new HttpInitializer( url, port, ssl ) ).remoteAddress( uri.getHost(), port ).connect().addListener( future );
    }
}
