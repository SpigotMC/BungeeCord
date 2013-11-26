package net.md_5.bungee.query;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;

@RequiredArgsConstructor
public class RemoteQuery
{

    private final ProxyServer bungee;
    private final ListenerInfo listener;

    public void start(InetSocketAddress address, EventLoopGroup eventLoop, ChannelFutureListener future)
    {
        new Bootstrap()
                .channel( NioDatagramChannel.class )
                .group( eventLoop )
                .handler( new QueryHandler( bungee, listener ) )
                .localAddress( address )
                .bind().addListener( future );
    }
}
