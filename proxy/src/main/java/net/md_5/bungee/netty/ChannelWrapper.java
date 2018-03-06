package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.compress.PacketCompressor;
import net.md_5.bungee.compress.PacketDecompressor;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;

public abstract class ChannelWrapper
{

    protected final Channel ch;
    @Getter
    @Setter
    protected InetSocketAddress remoteAddress;
    @Getter
    protected volatile boolean closed;
    @Getter
    protected volatile boolean closing;

    public ChannelWrapper(ChannelHandlerContext ctx)
    {
        this.ch = ctx.channel();
        this.remoteAddress = (InetSocketAddress) this.ch.remoteAddress();
    }

    public void setProtocol(Protocol protocol)
    {
        ch.pipeline().get( MinecraftDecoder.class ).setProtocol( protocol );
        ch.pipeline().get( MinecraftEncoder.class ).setProtocol( protocol );
    }

    public void setVersion(int protocol)
    {
        ch.pipeline().get( MinecraftDecoder.class ).setProtocolVersion( protocol );
        ch.pipeline().get( MinecraftEncoder.class ).setProtocolVersion( protocol );
    }

    public void write(Object packet)
    {
        if ( !closed )
        {
            if ( packet instanceof PacketWrapper )
            {
                ( (PacketWrapper) packet ).setReleased( true );
                ch.writeAndFlush( ( (PacketWrapper) packet ).buf, ch.voidPromise() );
            } else
            {
                ch.writeAndFlush( packet, ch.voidPromise() );
            }
        }
    }

    /**
     * Marks the closed dirty flag to true.
     */
    public void markClosed()
    {
        closed = closing = true;
    }

    protected void closeChannel()
    {
        ch.close();
    }

    public void close()
    {
        if ( !closed )
        {
            closed = closing = true;

            ch.flush();
            closeChannel();
        }
    }

    public void delayedClose()
    {
        if ( !closing )
        {
            closing = true;

            // Minecraft client can take some time to switch protocols.
            // Sending the wrong disconnect packet whilst a protocol switch is in progress will crash it.
            // Delay 250ms to ensure that the protocol switch (if any) has definitely taken place.
            ch.eventLoop().schedule( new Runnable()
            {

                @Override
                public void run()
                {
                    closeChannel();
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
        if ( ch.pipeline().get( PacketCompressor.class ) == null && compressionThreshold != -1 )
        {
            addBefore( PipelineUtils.PACKET_ENCODER, "compress", new PacketCompressor() );
        }
        if ( compressionThreshold != -1 )
        {
            ch.pipeline().get( PacketCompressor.class ).setThreshold( compressionThreshold );
        } else
        {
            ch.pipeline().remove( "compress" );
        }

        if ( ch.pipeline().get( PacketDecompressor.class ) == null && compressionThreshold != -1 )
        {
            addBefore( PipelineUtils.PACKET_DECODER, "decompress", new PacketDecompressor() );
        }
        if ( compressionThreshold == -1 )
        {
            ch.pipeline().remove( "decompress" );
        }
    }
}
