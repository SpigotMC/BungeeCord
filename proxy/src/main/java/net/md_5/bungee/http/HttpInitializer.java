package net.md_5.bungee.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;

@RequiredArgsConstructor
public class HttpInitializer extends ChannelInitializer<Channel>
{

    private final Callback<String> callback;
    private final boolean ssl;

    @Override
    protected void initChannel(Channel ch) throws Exception
    {
        ch.pipeline().addLast( "timeout", new ReadTimeoutHandler( HttpClient.TIMEOUT, TimeUnit.MILLISECONDS ) );
        if ( ssl )
        {
            SSLContext context = SSLContext.getInstance( "TLS" );
            context.init( null, new TrustManager[]
            {
                TrustingX509Manager.getInstance()
            }, null );

            SSLEngine engine = context.createSSLEngine();
            engine.setUseClientMode( true );

            ch.pipeline().addLast( "ssl", new SslHandler( engine ) );
        }
        ch.pipeline().addLast( "http", new HttpClientCodec() );
        ch.pipeline().addLast( "handler", new HttpHandler( callback ) );
    }
}
