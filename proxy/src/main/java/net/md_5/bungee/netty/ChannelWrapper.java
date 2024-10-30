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
import net.md_5.bungee.compress.PacketCompressor;
import net.md_5.bungee.compress.PacketDecompressor;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.Varint21LengthFieldPrepender;
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

    public ChannelWrapper(ChannelHandlerContext ctx)
    {
        this.ch = ctx.channel();
        this.remoteAddress = ( this.ch.remoteAddress() == null ) ? this.ch.parent().localAddress() : this.ch.remoteAddress();
    }

    public Protocol getDecodeProtocol()
    {
        return ch.pipeline().get( MinecraftDecoder.class ).getProtocol();
    }

    public void setDecodeProtocol(Protocol protocol)
    {
        ch.pipeline().get( MinecraftDecoder.class ).setProtocol( protocol );
    }

    public Protocol getEncodeProtocol()
    {
        return ch.pipeline().get( MinecraftEncoder.class ).getProtocol();

    }

    public void setEncodeProtocol(Protocol protocol)
    {
        ch.pipeline().get( MinecraftEncoder.class ).setProtocol( protocol );
    }

    public void setProtocol(Protocol protocol)
    {
        setDecodeProtocol( protocol );
        setEncodeProtocol( protocol );
    }

    public void setVersion(int protocol)
    {
        ch.pipeline().get( MinecraftDecoder.class ).setProtocolVersion( protocol );
        ch.pipeline().get( MinecraftEncoder.class ).setProtocolVersion( protocol );
    }

    public int getEncodeVersion()
    {
        return ch.pipeline().get( MinecraftEncoder.class ).getProtocolVersion();
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
        if ( ch.pipeline().get( PacketCompressor.class ) == null && compressionThreshold >= 0 )
        {
            addBefore( PipelineUtils.PACKET_ENCODER, "compress", new PacketCompressor() );
        }
        if ( compressionThreshold >= 0 )
        {
            ch.pipeline().get( PacketCompressor.class ).setThreshold( compressionThreshold );
        } else
        {
            ch.pipeline().remove( "compress" );
        }

        if ( ch.pipeline().get( PacketDecompressor.class ) == null && compressionThreshold >= 0 )
        {
            addBefore( PipelineUtils.PACKET_DECODER, "decompress", new PacketDecompressor() );
        }
        if ( compressionThreshold < 0 )
        {
            ch.pipeline().remove( "decompress" );
        }
        // disable use of composite buffers if we use natives
        updateComposite();
    }

    /*
     * Should be called on encryption add and on compressor add or remove
     */
    public void updateComposite()
    {
        CipherEncoder cipherEncoder = ch.pipeline().get( CipherEncoder.class );
        PacketCompressor packetCompressor = ch.pipeline().get( PacketCompressor.class );
        Varint21LengthFieldPrepender prepender = ch.pipeline().get( Varint21LengthFieldPrepender.class );
        boolean compressorCompose = cipherEncoder == null || cipherEncoder.getCipher().allowComposite();
        boolean prependerCompose = compressorCompose && ( packetCompressor == null || packetCompressor.getZlib().allowComposite() );

        if ( prepender != null )
        {
            ProxyServer.getInstance().getLogger().log( Level.FINE, "set prepender compose to {0} for {1}", new Object[]
            {
                prependerCompose, ch
            } );
            prepender.setCompose( prependerCompose );
        }
        if ( packetCompressor != null )
        {
            ProxyServer.getInstance().getLogger().log( Level.FINE, "set packetCompressor compose to {0} for {1}", new Object[]
            {
                compressorCompose, ch
            } );
            packetCompressor.setCompose( compressorCompose );
        }
    }
}
