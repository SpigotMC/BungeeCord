package net.md_5.bungee.netty;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.PlatformDependent;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.protocol.KickStringWriter;
import net.md_5.bungee.protocol.LegacyDecoder;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.Varint21FrameDecoder;
import net.md_5.bungee.protocol.Varint21LengthFieldPrepender;

public class PipelineUtils
{

    public static final AttributeKey<ListenerInfo> LISTENER = AttributeKey.valueOf( "ListerInfo" );
    public static final AttributeKey<UserConnection> USER = AttributeKey.valueOf( "User" );
    public static final AttributeKey<BungeeServerInfo> TARGET = AttributeKey.valueOf( "Target" );
    public static final ChannelInitializer<Channel> SERVER_CHILD = new ChannelInitializer<Channel>()
    {
        @Override
        protected void initChannel(Channel ch) throws Exception
        {
            ListenerInfo listener = ch.attr( LISTENER ).get();

            BASE.initChannel( ch );
            ch.pipeline().addBefore( FRAME_DECODER, LEGACY_DECODER, new LegacyDecoder() );
            ch.pipeline().addAfter( FRAME_DECODER, PACKET_DECODER, new MinecraftDecoder( Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion() ) );
            ch.pipeline().addAfter( FRAME_PREPENDER, PACKET_ENCODER, new MinecraftEncoder( Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion() ) );
            ch.pipeline().addBefore( FRAME_PREPENDER, LEGACY_KICKER, new KickStringWriter() );
            ch.pipeline().get( HandlerBoss.class ).setHandler( new InitialHandler( BungeeCord.getInstance(), listener ) );

            if ( listener.isProxyProtocol() )
            {
                ch.pipeline().addFirst( new HAProxyMessageDecoder() );
            }
        }
    };
    public static final Base BASE = new Base();
    private static final Varint21LengthFieldPrepender framePrepender = new Varint21LengthFieldPrepender();
    public static final String TIMEOUT_HANDLER = "timeout";
    public static final String PACKET_DECODER = "packet-decoder";
    public static final String PACKET_ENCODER = "packet-encoder";
    public static final String BOSS_HANDLER = "inbound-boss";
    public static final String ENCRYPT_HANDLER = "encrypt";
    public static final String DECRYPT_HANDLER = "decrypt";
    public static final String FRAME_DECODER = "frame-decoder";
    public static final String FRAME_PREPENDER = "frame-prepender";
    public static final String LEGACY_DECODER = "legacy-decoder";
    public static final String LEGACY_KICKER = "legacy-kick";

    private static boolean epoll;

    static
    {
        if ( !PlatformDependent.isWindows() && Boolean.parseBoolean( System.getProperty( "bungee.epoll", "true" ) ) )
        {
            ProxyServer.getInstance().getLogger().info( "Not on Windows, attempting to use enhanced EpollEventLoop" );

            if ( epoll = Epoll.isAvailable() )
            {
                ProxyServer.getInstance().getLogger().info( "Epoll is working, utilising it!" );
            } else
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Epoll is not working, falling back to NIO: {0}", Util.exception( Epoll.unavailabilityCause() ) );
            }
        }
    }

    public static EventLoopGroup newEventLoopGroup(int threads, ThreadFactory factory)
    {
        return epoll ? new EpollEventLoopGroup( threads, factory ) : new NioEventLoopGroup( threads, factory );
    }

    public static Class<? extends ServerChannel> getServerChannel()
    {
        return epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public static Class<? extends Channel> getChannel()
    {
        return epoll ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    public static Class<? extends Channel> getDatagramChannel()
    {
        return epoll ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }

    public final static class Base extends ChannelInitializer<Channel>
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
            ch.config().setAllocator( PooledByteBufAllocator.DEFAULT );

            ch.pipeline().addLast( TIMEOUT_HANDLER, new ReadTimeoutHandler( BungeeCord.getInstance().config.getTimeout(), TimeUnit.MILLISECONDS ) );
            ch.pipeline().addLast( FRAME_DECODER, new Varint21FrameDecoder() );
            ch.pipeline().addLast( FRAME_PREPENDER, framePrepender );

            ch.pipeline().addLast( BOSS_HANDLER, new HandlerBoss() );
        }
    }
}
