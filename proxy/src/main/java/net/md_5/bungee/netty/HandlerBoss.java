package net.md_5.bungee.netty;

import net.md_5.bungee.protocol.PacketWrapper;
import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.codec.DecoderException;
import java.io.IOException;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.PingHandler;
import net.md_5.bungee.protocol.BadPacketException;
import net.md_5.bungee.protocol.OverflowPacketException;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundHandlerAdapter
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
            channel = new ChannelWrapper( ctx );
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if ( handler != null )
        {
            PacketWrapper packet = (PacketWrapper) msg;
            boolean sendPacket = handler.shouldHandle( packet );
            try
            {
                if ( sendPacket && packet.packet != null )
                {
                    try
                    {
                        packet.packet.handle( handler );
                    } catch ( CancelSendSignal ex )
                    {
                        sendPacket = false;
                    }
                }
                if ( sendPacket )
                {
                    handler.handle( packet );
                }
            } finally
            {
                packet.trySingleRelease();
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
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - read timed out", handler );
            } else if ( cause instanceof DecoderException && cause.getCause() instanceof BadPacketException )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - bad packet ID, are mods in use!? {1}", new Object[]
                {
                    handler, cause.getCause().getMessage()
                } );
            } else if ( cause instanceof DecoderException && cause.getCause() instanceof OverflowPacketException )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - overflow in packet detected! {1}", new Object[]
                {
                    handler, cause.getCause().getMessage()
                } );
            } else if ( cause instanceof IOException )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - IOException: {1}", new Object[]
                {
                    handler, cause.getMessage()
                } );
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
