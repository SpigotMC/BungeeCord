package net.md_5.bungee.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.protocol.PacketDefinitions;

public class PipelineUtils
{

    public static final AttributeKey<ListenerInfo> LISTENER = new AttributeKey<>( "ListerInfo" );
    public static final ChannelInitializer<Channel> SERVER_CHILD = new ChannelInitializer<Channel>()
    {
        @Override
        protected void initChannel(Channel ch) throws Exception
        {
            BASE.initChannel( ch );
            ch.pipeline().get( HandlerBoss.class ).setHandler( new InitialHandler( ProxyServer.getInstance(), ch.attr( LISTENER ).get() ) );
        }
    };
    private static final Base BASE = new Base();

    private final static class Base extends ChannelInitializer<Channel>
    {

        @Override
        public void initChannel(Channel ch) throws Exception
        {
            try
            {
                ch.config().setOption( ChannelOption.IP_TOS, 0x18 );
            } catch ( ChannelException ex )
            {
                // IP_TOS is not supported (Windows XP / Windows Server 2003)
            }
            ch.pipeline().addLast( "timer", new ReadTimeoutHandler( BungeeCord.getInstance().config.getTimeout(), TimeUnit.MILLISECONDS ) );
            ch.pipeline().addLast( "decoder", new PacketDecoder( PacketDefinitions.VANILLA_PROTOCOL ) );
            ch.pipeline().addLast( "handler", new HandlerBoss() );
        }
    };

    public static ChannelFuture connectClient(SocketAddress remoteAddress)
    {
        return new Bootstrap()
                .channel( NioSocketChannel.class )
                .group( BungeeCord.getInstance().eventLoops )
                .handler( BASE )
                .remoteAddress( remoteAddress )
                .connect();
    }
}
