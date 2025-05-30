package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringDatagramChannel;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.channel.uring.IoUringServerSocketChannel;
import io.netty.channel.uring.IoUringSocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.PlatformDependent;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.ClientConnectEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.protocol.KickStringWriter;
import net.md_5.bungee.protocol.LegacyDecoder;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.Varint21FrameDecoder;
import net.md_5.bungee.protocol.channel.BungeeChannelInitializer;
import net.md_5.bungee.protocol.channel.ChannelAcceptor;
import net.md_5.bungee.util.PacketLimiter;

public class PipelineUtils
{

    public static final AttributeKey<ListenerInfo> LISTENER = AttributeKey.valueOf( "ListerInfo" );

    private static void setChannelInitializerHolders()
    {
        ProxyServer.getInstance().unsafe().setFrontendChannelInitializer( BungeeChannelInitializer.create( ch ->
        {
            SocketAddress remoteAddress = ( ch.remoteAddress() == null ) ? ch.parent().localAddress() : ch.remoteAddress();

            if ( BungeeCord.getInstance().getConnectionThrottle() != null && BungeeCord.getInstance().getConnectionThrottle().throttle( remoteAddress ) )
            {
                return false;
            }

            ListenerInfo listener = ch.attr( LISTENER ).get();

            if ( BungeeCord.getInstance().getPluginManager().callEvent( new ClientConnectEvent( remoteAddress, listener ) ).isCancelled() )
            {
                return false;
            }

            BASE.accept( ch );
            ch.pipeline().addBefore( FRAME_DECODER, LEGACY_DECODER, new LegacyDecoder() );
            ch.pipeline().addAfter( FRAME_DECODER, PACKET_DECODER, new MinecraftDecoder( Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion() ) );
            ch.pipeline().addAfter( FRAME_PREPENDER_AND_COMPRESS, PACKET_ENCODER, new MinecraftEncoder( Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion() ) );
            ch.pipeline().addBefore( FRAME_PREPENDER_AND_COMPRESS, LEGACY_KICKER, legacyKicker );

            HandlerBoss handlerBoss = ch.pipeline().get( HandlerBoss.class );
            handlerBoss.setHandler( new InitialHandler( BungeeCord.getInstance(), listener ) );

            int packetLimit = BungeeCord.getInstance().getConfig().getMaxPacketsPerSecond();
            int packetDataLimit = BungeeCord.getInstance().getConfig().getMaxPacketDataPerSecond();
            if ( packetLimit > 0 || packetDataLimit > 0 )
            {
                handlerBoss.setLimiter( new PacketLimiter( packetLimit, packetDataLimit ) );
            }
            if ( listener.isProxyProtocol() )
            {
                ch.pipeline().addFirst( new HAProxyMessageDecoder() );
            }

            return true;
        } ) );

        ProxyServer.getInstance().unsafe().setBackendChannelInitializer( BungeeChannelInitializer.create( ch ->
        {
            PipelineUtils.BASE_SERVERSIDE.accept( ch );
            ch.pipeline().addAfter( PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder( Protocol.HANDSHAKE, false, ProxyServer.getInstance().getProtocolVersion() ) );
            ch.pipeline().addAfter( PipelineUtils.FRAME_PREPENDER_AND_COMPRESS, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder( Protocol.HANDSHAKE, false, ProxyServer.getInstance().getProtocolVersion() ) );

            return true;
        } ) );

        ProxyServer.getInstance().unsafe().setServerInfoChannelInitializer( BungeeChannelInitializer.create( BASE_SERVERSIDE ) );
    }

    private static final ChannelAcceptor BASE = new Base( false );
    private static final ChannelAcceptor BASE_SERVERSIDE = new Base( true );
    private static final KickStringWriter legacyKicker = new KickStringWriter();
    public static final String TIMEOUT_HANDLER = "timeout";
    public static final String WRITE_TIMEOUT_HANDLER = "write-timeout";
    public static final String PACKET_DECODER = "packet-decoder";
    public static final String PACKET_ENCODER = "packet-encoder";
    public static final String BOSS_HANDLER = "inbound-boss";
    public static final String ENCRYPT_HANDLER = "encrypt";
    public static final String DECRYPT_HANDLER = "decrypt";
    public static final String FRAME_DECODER = "frame-decoder";
    public static final String FRAME_PREPENDER_AND_COMPRESS = "frame-prepender-compress";
    public static final String LEGACY_DECODER = "legacy-decoder";
    public static final String LEGACY_KICKER = "legacy-kick";

    private static boolean epoll;
    private static boolean io_uring;

    static
    {
        if ( !PlatformDependent.isWindows() )
        {
            // disable by default
            // TODO: maybe make it the new default?
            if ( Boolean.parseBoolean( System.getProperty( "bungee.io_uring", "false" ) ) )
            {
                ProxyServer.getInstance().getLogger().info( "Not on Windows, attempting to use enhanced IOUringEventLoopGroup" );
                if ( io_uring = IoUring.isAvailable() )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "io_uring is enabled and working, utilising it! (experimental feature)" );
                } else
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "io_uring is not working: {0}", Util.exception( IoUring.unavailabilityCause() ) );
                }
            }

            if ( !io_uring && Boolean.parseBoolean( System.getProperty( "bungee.epoll", "true" ) ) )
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

        setChannelInitializerHolders();
    }

    public static EventLoopGroup newEventLoopGroup(int threads, ThreadFactory factory)
    {
        return new MultiThreadIoEventLoopGroup( threads, factory, io_uring ? IoUringIoHandler.newFactory() : epoll ? EpollIoHandler.newFactory() : NioIoHandler.newFactory() );
    }

    public static Class<? extends ServerChannel> getServerChannel(SocketAddress address)
    {
        if ( address instanceof DomainSocketAddress )
        {
            Preconditions.checkState( epoll, "Epoll required to have UNIX sockets" );

            return EpollServerDomainSocketChannel.class;
        }

        return io_uring ? IoUringServerSocketChannel.class : epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public static Class<? extends Channel> getChannel(SocketAddress address)
    {
        if ( address instanceof DomainSocketAddress )
        {
            Preconditions.checkState( epoll, "Epoll required to have UNIX sockets" );

            return EpollDomainSocketChannel.class;
        }

        return io_uring ? IoUringSocketChannel.class : epoll ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    public static Class<? extends DatagramChannel> getDatagramChannel()
    {
        return io_uring ? IoUringDatagramChannel.class : epoll ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }

    private static final int LOW_MARK = Integer.getInteger( "net.md_5.bungee.low_mark", 2 << 18 ); // 0.5 mb
    private static final int HIGH_MARK = Integer.getInteger( "net.md_5.bungee.high_mark", 2 << 20 ); // 2 mb
    private static final WriteBufferWaterMark MARK = new WriteBufferWaterMark( LOW_MARK, HIGH_MARK );

    @NoArgsConstructor // for backwards compatibility
    @AllArgsConstructor
    public static final class Base implements ChannelAcceptor
    {

        private boolean toServer = false;

        @Override
        public boolean accept(Channel ch)
        {
            try
            {
                ch.config().setOption( ChannelOption.IP_TOS, 0x18 );
            } catch ( ChannelException ex )
            {
                // IP_TOS is not supported (Windows XP / Windows Server 2003)
            }
            ch.config().setWriteBufferWaterMark( MARK );

            ch.pipeline().addLast( FRAME_DECODER, new Varint21FrameDecoder() );
            ch.pipeline().addLast( TIMEOUT_HANDLER, new ReadTimeoutHandler( BungeeCord.getInstance().config.getTimeout(), TimeUnit.MILLISECONDS ) );
            ch.pipeline().addLast( WRITE_TIMEOUT_HANDLER, new WriteTimeoutHandler( BungeeCord.getInstance().config.getTimeout(), TimeUnit.MILLISECONDS ) );
            // No encryption bungee -> server, therefore use extra buffer to avoid copying everything for length prepending
            // Not used bungee -> client as header would need to be encrypted separately through expensive JNI call
            // TODO: evaluate difference compose vs two buffers
            ch.pipeline().addLast( FRAME_PREPENDER_AND_COMPRESS, new LengthPrependerAndCompressor( true, toServer ) );
            ch.pipeline().addLast( BOSS_HANDLER, new HandlerBoss() );

            return true;
        }
    }
}
