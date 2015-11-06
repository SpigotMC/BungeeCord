package net.md_5.bungee.http;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.netty.PipelineUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpClient
{

    public static final int TIMEOUT = 5000;
    private static final Cache<String, InetAddress> addressCache = CacheBuilder.newBuilder().expireAfterWrite( 1, TimeUnit.MINUTES ).build();

    @SuppressWarnings("UnusedAssignment")
    public static void get(String url, EventLoop eventLoop, final Callback<String> callback)
    {
        Preconditions.checkNotNull( url, "url" );
        Preconditions.checkNotNull( eventLoop, "eventLoop" );
        Preconditions.checkNotNull( callback, "callBack" );

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

        InetAddress inetHost = addressCache.getIfPresent( uri.getHost() );
        if ( inetHost == null )
        {
            try
            {
                inetHost = InetAddress.getByName( uri.getHost() );
            } catch ( UnknownHostException ex )
            {
                callback.done( null, ex );
                return;
            }
            addressCache.put( uri.getHost(), inetHost );
        }

        ChannelFutureListener future = new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( future.isSuccess() )
                {
                    String path = uri.getRawPath() + ( ( uri.getRawQuery() == null ) ? "" : "?" + uri.getRawQuery() );

                    HttpRequest request = new DefaultHttpRequest( HttpVersion.HTTP_1_1, HttpMethod.GET, path );
                    request.headers().set( HttpHeaders.Names.HOST, uri.getHost() );

                    future.channel().writeAndFlush( request );
                } else
                {
                    callback.done( null, future.cause() );
                }
            }
        };

        new Bootstrap().channel( PipelineUtils.getChannel() ).group( eventLoop ).handler( new HttpInitializer( callback, ssl, uri.getHost(), port ) ).
                option( ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT ).remoteAddress( inetHost, port ).connect().addListener( future );
    }
}
