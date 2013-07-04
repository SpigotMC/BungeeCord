package net.md_5.bungee.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import java.nio.charset.Charset;

public class HttpHandler extends SimpleChannelInboundHandler<HttpObject>
{

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception
    {
        if ( msg instanceof HttpResponse )
        {
            HttpResponse response = (HttpResponse) msg;
            if ( response.getStatus() != HttpResponseStatus.OK )
            {
            }
        }
        if ( msg instanceof HttpContent )
        {
            HttpContent content = (HttpContent) msg;
            String s = content.content().toString( Charset.forName( "UTF-8" ) );

            if ( msg instanceof LastHttpContent )
            {
                ctx.channel().close();
            }
        }
    }
}
