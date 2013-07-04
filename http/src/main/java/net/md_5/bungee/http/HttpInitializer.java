package net.md_5.bungee.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslHandler;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpInitializer extends ChannelInitializer<Channel>
{

    private final String host;
    private final int port;
    private final boolean ssl;

    @Override
    protected void initChannel(Channel ch) throws Exception
    {
        if ( ssl )
        {
            SSLContext context = SSLContext.getDefault();
            SSLEngine engine = context.createSSLEngine( host, port );
            ch.pipeline().addLast( "ssl", new SslHandler( engine ) );
        }
        ch.pipeline().addLast( "http", new HttpClientCodec() );
        ch.pipeline().addLast( "handler", new HttpHandler() );
    }
}
