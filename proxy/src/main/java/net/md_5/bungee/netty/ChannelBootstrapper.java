package net.md_5.bungee.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.InitialHandler;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.packet.PacketHandler;
import net.md_5.bungee.protocol.PacketDefinitions;

public class ChannelBootstrapper extends ChannelInitializer<Channel>
{

    public static ChannelBootstrapper CLIENT = new ChannelBootstrapper( InitialHandler.class );
    public static ChannelBootstrapper SERVER = new ChannelBootstrapper( ServerConnector.class );
    private final Constructor<? extends PacketHandler> initial;

    private ChannelBootstrapper(Class<? extends PacketHandler> initialHandler)
    {
        try
        {
            this.initial = initialHandler.getDeclaredConstructor();
        } catch ( NoSuchMethodException ex )
        {
            throw new ExceptionInInitializerError( ex );
        }
    }

    @Override
    protected void initChannel(Channel ch) throws Exception
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

        ch.pipeline().addLast( "handler", new HandlerBoss( initial.newInstance() ) );
    }
}
