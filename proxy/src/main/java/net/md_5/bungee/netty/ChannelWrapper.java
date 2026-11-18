package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.compress.PacketDecompressor;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Kick;

public class ChannelWrapper
{

    private final Channel ch;
    @Getter
    @Setter
    private SocketAddress remoteAddress;
    @Getter
    private volatile boolean closed;
    @Getter
    private volatile boolean closing;
    /**
     * Compression threshold which has been announced by the remote, but which
     * is not in use yet (26.4+).
     */
    @Getter
    @Setter
    private int pendingCompressionThreshold = -1;

    public ChannelWrapper(ChannelHandlerContext ctx)
    {
        this.ch = ctx.channel();
        this.remoteAddress = ( this.ch.remoteAddress() == null ) ? this.ch.parent().localAddress() : this.ch.remoteAddress();
    }

    public Protocol getDecodeProtocol()
    {
        return getMinecraftDecoder().getProtocol();
    }

    public void setDecodeProtocol(Protocol protocol)
    {
        getMinecraftDecoder().setProtocol( protocol );
    }

    public Protocol getEncodeProtocol()
    {
        return getMinecraftEncoder().getProtocol();
    }

    public void setEncodeProtocol(Protocol protocol)
    {
        getMinecraftEncoder().setProtocol( protocol );
    }

    public void setProtocol(Protocol protocol)
    {
        setDecodeProtocol( protocol );
        setEncodeProtocol( protocol );
    }

    public void setVersion(int protocol)
    {
        getMinecraftDecoder().setProtocolVersion( protocol );
        getMinecraftEncoder().setProtocolVersion( protocol );
    }

    public MinecraftDecoder getMinecraftDecoder()
    {
        return ch.pipeline().get( MinecraftDecoder.class );
    }

    public MinecraftEncoder getMinecraftEncoder()
    {
        return ch.pipeline().get( MinecraftEncoder.class );
    }

    public int getEncodeVersion()
    {
        return getMinecraftEncoder().getProtocolVersion();
    }

    public void write(Object packet)
    {
        if ( !closed )
        {
            DefinedPacket defined = null;
            if ( packet instanceof PacketWrapper )
            {
                PacketWrapper wrapper = (PacketWrapper) packet;
                wrapper.setReleased( true );
                ch.writeAndFlush( wrapper.buf, ch.voidPromise() );
                defined = wrapper.packet;
            } else
            {
                ch.writeAndFlush( packet, ch.voidPromise() );
                if ( packet instanceof DefinedPacket )
                {
                    defined = (DefinedPacket) packet;
                }
            }

            if ( defined != null )
            {
                Protocol nextProtocol = defined.nextProtocol();
                if ( nextProtocol != null )
                {
                    setEncodeProtocol( nextProtocol );
                }
            }
        }
    }

    public void markClosed()
    {
        closed = closing = true;
    }

    public void close()
    {
        close( null );
    }

    public void close(Object packet)
    {
        if ( !closed )
        {
            if ( !closing )
            {
                ch.config().setAutoRead( false );
            }

            closed = closing = true;

            if ( packet != null && ch.isActive() )
            {
                ch.writeAndFlush( packet ).addListeners( ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE, ChannelFutureListener.CLOSE );
            } else
            {
                ch.flush();
                ch.close();
            }
        }
    }

    public void delayedClose(final Kick kick)
    {
        if ( !closing )
        {
            closing = true;
            ch.config().setAutoRead( false );

            // Minecraft client can take some time to switch protocols.
            // Sending the wrong disconnect packet whilst a protocol switch is in progress will crash it.
            // Delay 250ms to ensure that the protocol switch (if any) has definitely taken place.
            ch.eventLoop().schedule( new Runnable()
            {

                @Override
                public void run()
                {
                    close( kick );
                }
            }, 250, TimeUnit.MILLISECONDS );
        }
    }

    public void addBefore(String baseName, String name, ChannelHandler handler)
    {
        Preconditions.checkState( ch.eventLoop().inEventLoop(), "cannot add handler outside of event loop" );
        ch.pipeline().flush();
        ch.pipeline().addBefore( baseName, name, handler );
    }

    public Channel getHandle()
    {
        return ch;
    }

    public void setCompressionThreshold(int compressionThreshold)
    {
        setOutboundCompressionThreshold( compressionThreshold );
        setInboundCompression( compressionThreshold >= 0 );
    }

    /**
     * Sets the compression threshold for packets we send.
     *
     * @param compressionThreshold the threshold, negative to disable
     */
    public void setOutboundCompressionThreshold(int compressionThreshold)
    {
        LengthPrependerAndCompressor compressor = ch.pipeline().get( LengthPrependerAndCompressor.class );
        if ( compressionThreshold >= 0 )
        {
            if ( !compressor.isCompress() )
            {
                compressor.setCompress( true );
            }
            compressor.setThreshold( compressionThreshold );
        } else
        {
            compressor.setCompress( false );
        }

        // disable use of composite buffers if we use natives
        updateComposite();
    }

    /**
     * Sets whether packets we receive are in the compressed format.
     *
     * @param compression whether to expect the compressed format
     */
    public void setInboundCompression(boolean compression)
    {
        PacketDecompressor decompressor = ch.pipeline().get( PacketDecompressor.class );
        if ( compression )
        {
            if ( decompressor == null )
            {
                addBefore( PipelineUtils.PACKET_DECODER, "decompress", new PacketDecompressor() );
            }
        } else if ( decompressor != null )
        {
            ch.pipeline().remove( "decompress" );
        }
    }

    /**
     * Starts to compress the packets we send with the threshold previously
     * announced by the remote, if any.
     */
    public void applyPendingOutboundCompression()
    {
        if ( pendingCompressionThreshold >= 0 )
        {
            setOutboundCompressionThreshold( pendingCompressionThreshold );
        }
    }

    /*
     * Should be called on encryption add and on compressor add or remove
     */
    public void updateComposite()
    {
        CipherEncoder cipherEncoder = ch.pipeline().get( CipherEncoder.class );
        LengthPrependerAndCompressor prependerAndCompressor = ch.pipeline().get( LengthPrependerAndCompressor.class );
        boolean compressorCompose = cipherEncoder == null || cipherEncoder.getCipher().allowComposite();

        if ( prependerAndCompressor != null )
        {
            ProxyServer.getInstance().getLogger().log( Level.FINE, "set prepender compose to {0} for {1}", new Object[]
            {
                compressorCompose, ch
            } );
            prependerAndCompressor.setCompose( compressorCompose );
        }
    }

    public void scheduleIfNecessary(Runnable task)
    {
        if ( ch.eventLoop().inEventLoop() )
        {
            task.run();
            return;
        }

        ch.eventLoop().submit( task ).addListener( future ->
        {
            if ( !future.isSuccess() )
            {
                ch.pipeline().fireExceptionCaught( future.cause() );
            }
        } );
    }
}
