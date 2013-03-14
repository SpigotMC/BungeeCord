package net.md_5.bungee.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.protocol.PacketDefinitions;

public class PipelineUtils
{

    public static final AttributeKey<ListenerInfo> LISTENER = new AttributeKey<>( "ListerInfo" );
    public static final AttributeKey<UserConnection> USER = new AttributeKey<>( "User" );
    public static final AttributeKey<ServerInfo> TARGET = new AttributeKey<>( "Target" );
    public static final ChannelInitializer<Channel> SERVER_CHILD = new ChannelInitializer<Channel>()
    {
        @Override
        protected void initChannel(Channel ch) throws Exception
        {
            BASE.initChannel( ch );
            ch.pipeline().get( HandlerBoss.class ).setHandler( new InitialHandler( ProxyServer.getInstance(), ch.attr( LISTENER ).get() ) );
        }
    };
    public static final ChannelInitializer<Channel> CLIENT = new ChannelInitializer<Channel>()
    {
        @Override
        protected void initChannel(Channel ch) throws Exception
        {
            BASE.initChannel( ch );
            ch.pipeline().get( HandlerBoss.class ).setHandler( new ServerConnector( ProxyServer.getInstance(), ch.attr( USER ).get(), ch.attr( TARGET ).get() ) );
        }
    };
    public static final Base BASE = new Base();
    private static final DefinedPacketEncoder packetEncoder = new DefinedPacketEncoder();
    private static final ByteArrayEncoder arrayEncoder = new ByteArrayEncoder();

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
            ch.pipeline().addLast( "timer", new ReadTimeoutHandler( BungeeCord.getInstance().config.getTimeout(), TimeUnit.MILLISECONDS ) );
            ch.pipeline().addLast( "decoder", new PacketDecoder( PacketDefinitions.VANILLA_PROTOCOL ) );
            ch.pipeline().addLast( "packet-encoder", packetEncoder );
            ch.pipeline().addLast( "array-encoder", arrayEncoder );
            ch.pipeline().addLast( "handler", new HandlerBoss() );
        }
    };
}
