package net.md_5.bungee.netty;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.UserConnection;
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

    public static final AttributeKey<ListenerInfo> LISTENER = new AttributeKey<>( "ListerInfo" );
    public static final AttributeKey<UserConnection> USER = new AttributeKey<>( "User" );
    public static final AttributeKey<BungeeServerInfo> TARGET = new AttributeKey<>( "Target" );
    public static final ChannelInitializer<Channel> SERVER_CHILD = new ChannelInitializer<Channel>()
    {
        @Override
        protected void initChannel(Channel ch) throws Exception
        {
            if ( BungeeCord.getInstance().getConnectionThrottle().throttle( ( (InetSocketAddress) ch.remoteAddress() ).getAddress() ) )
            {
                // TODO: Better throttle - we can't throttle this way if we want to maintain 1.7 compat!
                // ch.close();
                // return;
            }

            BASE.initChannel( ch );
            ch.pipeline().addBefore( FRAME_DECODER, LEGACY_DECODER, new LegacyDecoder() );
            ch.pipeline().addAfter( FRAME_DECODER, PACKET_DECODER, new MinecraftDecoder( Protocol.HANDSHAKE, true ) );
            ch.pipeline().addAfter( FRAME_PREPENDER, PACKET_ENCODER, new MinecraftEncoder( Protocol.HANDSHAKE, true ) );
            ch.pipeline().addBefore( FRAME_PREPENDER, LEGACY_KICKER, new KickStringWriter() );
            ch.pipeline().get( HandlerBoss.class ).setHandler( new InitialHandler( ProxyServer.getInstance(), ch.attr( LISTENER ).get() ) );
        }
    };
    public static final Base BASE = new Base();
    private static final Varint21LengthFieldPrepender framePrepender = new Varint21LengthFieldPrepender();
    public static String TIMEOUT_HANDLER = "timeout";
    public static String PACKET_DECODER = "packet-decoder";
    public static String PACKET_ENCODER = "packet-encoder";
    public static String BOSS_HANDLER = "inbound-boss";
    public static String ENCRYPT_HANDLER = "encrypt";
    public static String DECRYPT_HANDLER = "decrypt";
    public static String FRAME_DECODER = "frame-decoder";
    public static String FRAME_PREPENDER = "frame-prepender";
    public static String LEGACY_DECODER = "legacy-decoder";
    public static String LEGACY_KICKER = "legacy-kick";

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
    };
}
