package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import java.io.IOException;
import java.util.logging.Level;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.PingHandler;
import net.md_5.bungee.connection.UpstreamBridge;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundMessageHandlerAdapter<Object>
{

    private ChannelWrapper channel;
    private PacketHandler handler;

    public void setHandler(PacketHandler handler)
    {
        Preconditions.checkArgument( handler != null, "handler" );
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            channel = new ChannelWrapper( ctx.channel() );
            handler.connected( channel );

            if ( !( handler instanceof InitialHandler || handler instanceof PingHandler ) )
            {
                ProxyServer.getInstance().getLogger().log( Level.INFO, "{0} has connected", handler );
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            handler.disconnected( channel );

            if ( !( handler instanceof InitialHandler || handler instanceof PingHandler ) )
            {
                ProxyServer.getInstance().getLogger().log( Level.INFO, "{0} has disconnected", handler );
            }
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if ( handler != null && ctx.channel().isActive() )
        {
            if ( msg instanceof PacketWrapper )
            {
                boolean sendPacket = true;
                try
                {
                    ( (PacketWrapper) msg ).packet.handle( handler );
                } catch ( CancelSendSignal ex )
                {
                    sendPacket = false;
                }
                if ( sendPacket )
                {
                    handler.handle( ( (PacketWrapper) msg ).buf );
                }
            } else
            {
                handler.handle( (byte[]) msg );
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        if ( ctx.channel().isActive() )
        {
            if ( cause instanceof ReadTimeoutException )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, handler + " - read timed out" );
            } else if ( cause instanceof IOException )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, handler + " - IOException: " + cause.getMessage() );
            } else
            {
                ProxyServer.getInstance().getLogger().log( Level.SEVERE, handler + " - encountered exception", cause );
            }
            if ( handler != null )
            {
                try
                {
                    handler.exception( cause );
                } catch ( Exception ex )
                {
                    ProxyServer.getInstance().getLogger().log( Level.SEVERE, handler + " - exception processing exception", ex );
                }
            }
            ctx.close();
        }
    }
}
