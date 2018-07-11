package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.compress.PacketCompressor;
import net.md_5.bungee.compress.PacketDecompressor;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.entitymap.EntityMap;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Kick;

public class ChannelWrapper
{

    private static final Map<CopiedBuffersCacheKey, boolean[]> COPIED_BUFFERS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Protocol.ProtocolData, boolean[]> HANDLED_PACKETS_CACHE = new ConcurrentHashMap<>();

    private final Channel ch;
    private PacketHandler handler;
    @Getter
    @Setter
    private InetSocketAddress remoteAddress;
    @Getter
    private volatile boolean closed;
    @Getter
    private volatile boolean closing;

    public ChannelWrapper(ChannelHandlerContext ctx)
    {
        this.ch = ctx.channel();
        this.remoteAddress = (InetSocketAddress) this.ch.remoteAddress();
    }

    public void setHandler(PacketHandler handler)
    {
        this.handler = handler;
        updateCopiedBuffers();
        updateHandledPackets();
    }

    public void setProtocol(Protocol protocol)
    {
        ch.pipeline().get( MinecraftDecoder.class ).setProtocol( protocol );
        ch.pipeline().get( MinecraftEncoder.class ).setProtocol( protocol );
        updateCopiedBuffers();
        updateHandledPackets();
    }

    public void setVersion(int protocol)
    {
        ch.pipeline().get( MinecraftDecoder.class ).setProtocolVersion( protocol );
        ch.pipeline().get( MinecraftEncoder.class ).setProtocolVersion( protocol );
        updateCopiedBuffers();
        updateHandledPackets();
    }

    private void updateCopiedBuffers()
    {
        MinecraftDecoder decoder = ch.pipeline().get( MinecraftDecoder.class );
        if ( handler == null )
        {
            decoder.setCopiedBuffers( null );
        } else
        {
            decoder.setCopiedBuffers( computeCopiedBuffers( handler, decoder.getProtocol(), decoder.isServer(), decoder.getProtocolVersion() ) );
        }
    }

    private void updateHandledPackets()
    {
        MinecraftDecoder decoder = ch.pipeline().get( MinecraftDecoder.class );
        if ( handler == null )
        {
            decoder.setHandledPackets( null );
        } else
        {
            decoder.setHandledPackets( computeHandledPackets( handler.getClass(), decoder.getProtocol(), decoder.isServer(), decoder.getProtocolVersion() ) );
        }
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
                ch.eventLoop().schedule( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ch.close();
                    }
                }, 250, TimeUnit.MILLISECONDS );
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

    private static boolean[] computeCopiedBuffers(PacketHandler handler, Protocol protocol, boolean server, int protocolVersion)
    {
        EntityMap entityMap;
        if ( handler instanceof UpstreamBridge )
        {
            entityMap = ( (UpstreamBridge) handler ).getCon().getEntityRewrite();
        } else if ( handler instanceof DownstreamBridge )
        {
            entityMap = ( (DownstreamBridge) handler ).getCon().getEntityRewrite();
        } else
        {
            return null;
        }
        CopiedBuffersCacheKey cacheKey = new CopiedBuffersCacheKey( entityMap.getClass(), server );
        boolean[] copiedBuffers = COPIED_BUFFERS_CACHE.get( cacheKey );
        if ( copiedBuffers == null )
        {
            copiedBuffers = new boolean[ Protocol.MAX_PACKET_ID ];
            for ( int i = 0; i < Protocol.MAX_PACKET_ID; i++ )
            {
                boolean varintRewritten = server ? entityMap.hasServerboundRewrite( i, true ) : entityMap.hasClientboundRewrite( i, true );
                if ( varintRewritten )
                {
                    copiedBuffers[i] = true;
                }
            }
            COPIED_BUFFERS_CACHE.put( cacheKey, copiedBuffers );
        }
        return copiedBuffers;
    }

    private static boolean[] computeHandledPackets(Class<? extends PacketHandler> handlerClass, Protocol protocol, boolean server, int protocolVersion)
    {
        Protocol.DirectionData protDir = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        Protocol.ProtocolData protData = protDir.getProtocolData( protocolVersion );
        boolean[] handledPackets = HANDLED_PACKETS_CACHE.get( protData );
        if ( handledPackets == null )
        {
            handledPackets = new boolean[ Protocol.MAX_PACKET_ID ];
            for ( int i = 0; i < Protocol.MAX_PACKET_ID; i++ )
            {
                Class<? extends DefinedPacket> packetClass = protData.getPacketClass( i );
                if ( packetClass != null )
                {
                    try
                    {
                        Method defaultMethod = PacketHandler.class.getMethod( "handle", packetClass );
                        Method handlerMethod = handlerClass.getMethod( "handle", packetClass );
                        if ( !defaultMethod.equals( handlerMethod ) )
                        {
                            handledPackets[i] = true;
                        }
                    } catch ( NoSuchMethodException ignored )
                    {
                    }
                }
            }
            HANDLED_PACKETS_CACHE.put( protData, handledPackets );
        }
        return handledPackets;
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class CopiedBuffersCacheKey
    {
        private final Class<? extends EntityMap> entityMapClass;
        private final boolean server;
    }
}
