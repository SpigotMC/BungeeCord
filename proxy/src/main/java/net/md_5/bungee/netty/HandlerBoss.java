package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.timeout.ReadTimeoutException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.PingHandler;
import net.md_5.bungee.protocol.BadPacketException;
import net.md_5.bungee.protocol.OverflowPacketException;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.util.QuietException;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundHandlerAdapter
{

    private static final boolean printAllStacktraces = Boolean.getBoolean( "botfilter.printallerrors" );

    private ChannelWrapper channel;
    private PacketHandler handler;
    public void setHandler(PacketHandler handler)
    {
        Preconditions.checkArgument( handler != null, "handler" );
        if ( this.handler != null )
        { //BotFilter start
            this.handler.handlerChanged();
        } // END
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
                ProxyServer.getInstance().getLogger().log( Level.INFO, "{0} has connected", handler.toString() ); // BotFilter, use toString() instead of object
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
                ProxyServer.getInstance().getLogger().log( Level.INFO, "{0} has disconnected", handler.toString() ); // BotFilter, use toString() instead of object
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
                }
            } finally
            {
                proxy.release();
            }
            return;
        }

        if ( handler != null )
        {
            PacketWrapper packet = (PacketWrapper) msg;
            boolean sendPacket = handler.shouldHandle( packet );
            try
            {
                if ( !channel.isClosed() ) //BotFilter Do not handle packets if closed
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
            if ( printAllStacktraces )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Exception in handler " + handler.toString(), cause );
            }
            boolean logExceptions = !( handler instanceof PingHandler );

            if ( logExceptions )
            {
                if ( cause instanceof ReadTimeoutException )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - read timed out", handler.toString() ); // BotFilter, use toString() instead of object
                } else if ( cause instanceof DecoderException )
                {
                    if ( cause instanceof CorruptedFrameException )
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - corrupted frame: {1}", new Object[]
                        {
                            handler.toString(), cause.getMessage() // BotFilter, use toString() instead of object
                        } );
                    } else if ( cause.getCause() instanceof BadPacketException )
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - bad packet, are mods in use!? {1}", new Object[]
                        {
                            handler.toString(), cause.getCause().getMessage() // BotFilter, use toString() instead of object
                        } );
                    } else if ( cause.getCause() instanceof OverflowPacketException )
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - overflow in packet detected! {1}", new Object[]
                        {
                            handler.toString(), cause.getCause().getMessage() // BotFilter, use toString() instead of object
                        } );
                    } else
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - could not decode packet! {1}", new Object[]
                        {
                            handler, cause.getCause() != null ? cause.getCause() : cause
                        } );
                    }
                } else if ( cause instanceof IOException || ( cause instanceof IllegalStateException && handler instanceof InitialHandler ) )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - {1}: {2}", new Object[]
                    {
                        handler.toString(), cause.getClass().getSimpleName(), cause.getMessage() // BotFilter, use toString() instead of object
                    } );
                } else if ( cause instanceof QuietException )
                {
                    ProxyServer.getInstance().getLogger().log( Level.SEVERE, "{0} - encountered exception: {1}", new Object[]
                    {
                        handler.toString(), cause // BotFilter, use toString() instead of object
                    } );
                } else
                {
                    ProxyServer.getInstance().getLogger().log( Level.SEVERE, handler.toString() + " - encountered exception", cause );  // BotFilter, use toString() instead of object
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

            channel.markClosed();
            ctx.close();
        }
    }
}
