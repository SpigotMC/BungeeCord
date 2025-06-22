package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.PingHandler;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.protocol.BadPacketException;
import net.md_5.bungee.protocol.OverflowPacketException;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.util.PacketLimiter;
import net.md_5.bungee.util.QuietException;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundHandlerAdapter
{

    @Setter
    private PacketLimiter limiter;
    private ChannelWrapper channel;
    private PacketHandler handler;
    private boolean healthCheck;

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
            channel.markClosed();
            handler.disconnected( channel );

            if ( !( handler instanceof InitialHandler || handler instanceof PingHandler ) )
            {
                ProxyServer.getInstance().getLogger().log( Level.INFO, "{0} has disconnected", handler );
            }
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            handler.writabilityChanged( channel );
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if ( msg instanceof HAProxyMessage )
        {
            HAProxyMessage proxy = (HAProxyMessage) msg;
            try
            {
                if ( proxy.sourceAddress() != null )
                {
                    InetSocketAddress newAddress = new InetSocketAddress( proxy.sourceAddress(), proxy.sourcePort() );

                    ProxyServer.getInstance().getLogger().log( Level.FINE, "Set remote address via PROXY {0} -> {1}", new Object[]
                    {
                        channel.getRemoteAddress(), newAddress
                    } );

                    channel.setRemoteAddress( newAddress );
                } else
                {
                    healthCheck = true;
                }
            } finally
            {
                proxy.release();
            }
            return;
        }

        PacketWrapper packet = (PacketWrapper) msg;

        try
        {
            // check if the player exceeds packet limits, put inside try final, so we always release.
            if ( limiter != null && !limiter.incrementAndCheck( packet.buf.readableBytes() ) )
            {
                // we shouldn't tell the player what limits he exceeds by default
                // but if someone applies custom message we should allow them to display counter and bytes
                channel.close( handler instanceof UpstreamBridge ? new Kick( TextComponent.fromLegacy( ProxyServer.getInstance().getTranslation( "packet_limit_kick", limiter.getCounter(), limiter.getDataCounter() ) ) ) : null );
                // but the server admin should know
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} exceeded packet limit ({1} packets and {2} bytes per second)", new Object[]
                {
                    handler, limiter.getCounter(), limiter.getDataCounter()
                } );
                return;
            }

            if ( packet.packet != null )
            {
                Protocol nextProtocol = packet.packet.nextProtocol();
                if ( nextProtocol != null )
                {
                    channel.setDecodeProtocol( nextProtocol );
                }
            }

            if ( handler != null )
            {
                boolean sendPacket = handler.shouldHandle( packet );
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
            }
        } finally
        {
            packet.trySingleRelease();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        if ( ctx.channel().isActive() )
        {
            boolean logExceptions = !( handler instanceof PingHandler ) && !healthCheck;

            if ( logExceptions )
            {
                if ( cause instanceof ReadTimeoutException )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - read timed out", handler );
                } else if ( cause instanceof WriteTimeoutException )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - write timed out", handler );
                } else if ( cause instanceof DecoderException )
                {
                    if ( cause instanceof CorruptedFrameException )
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - corrupted frame: {1}", new Object[]
                        {
                            handler, cause.getMessage()
                        } );
                    } else if ( cause.getCause() instanceof BadPacketException )
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - bad packet, are mods in use!? {1}", new Object[]
                        {
                            handler, cause.getCause().getMessage()
                        } );
                    } else if ( cause.getCause() instanceof OverflowPacketException )
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - overflow in packet detected! {1}", new Object[]
                        {
                            handler, cause.getCause().getMessage()
                        } );
                    } else
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, handler + " - could not decode packet!", cause );
                    }
                } else if ( cause instanceof IOException || ( cause instanceof IllegalStateException && handler instanceof InitialHandler ) )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - {1}: {2}", new Object[]
                    {
                        handler, cause.getClass().getSimpleName(), cause.getMessage()
                    } );
                } else if ( cause instanceof QuietException )
                {
                    ProxyServer.getInstance().getLogger().log( Level.SEVERE, "{0} - encountered exception: {1}", new Object[]
                    {
                        handler, cause
                    } );
                } else
                {
                    ProxyServer.getInstance().getLogger().log( Level.SEVERE, handler + " - encountered exception", cause );
                }
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
