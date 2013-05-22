package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.PacketHandler;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundMessageHandlerAdapter<byte[]>
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
            ProxyServer.getInstance().getLogger().log( Level.INFO, "{0} has connected", handler );
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            ProxyServer.getInstance().getLogger().log( Level.INFO, "{0} has disconnected", handler );
            handler.disconnected( channel );
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, byte[] msg) throws Exception
    {
        if ( handler != null && ctx.channel().isActive() )
        {
            DefinedPacket packet = DefinedPacket.packet( msg );
            boolean sendPacket = true;
            if ( packet != null )
            {
                try
                {
                    packet.handle( handler );
                } catch ( CancelSendSignal ex )
                {
                    sendPacket = false;
                }
            }
            if ( sendPacket )
            {
                handler.handle( msg );
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
            } else if ( !( cause instanceof ClosedChannelException ) )
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
