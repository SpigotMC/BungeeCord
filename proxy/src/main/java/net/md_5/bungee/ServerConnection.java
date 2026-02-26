package net.md_5.bungee;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelFutureListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.FinishConfiguration;
import net.md_5.bungee.protocol.packet.PluginMessage;

@RequiredArgsConstructor
public class ServerConnection implements Server
{

    @Getter
    private final ChannelWrapper ch;
    @Getter
    private final BungeeServerInfo info;
    @Getter
    @Setter
    private boolean isObsolete;
    @Getter
    private final boolean forgeServer = false;
    @Getter
    private final Queue<KeepAliveData> keepAlives = new ArrayDeque<>();
    private final Queue<DefinedPacket> packetQueue = new ArrayDeque<>();
    // This should only be accessed inside this connections event loop to prevent race conditions
    private Queue<PacketWrapper> configQueue;
    private boolean receivedConfigFinish;

    public boolean isQueuingConfigPackets()
    {
        Preconditions.checkState( ch.getHandle().eventLoop().inEventLoop(), "not in event loop" );
        return configQueue != null;
    }

    public void onConfigFinished(UserConnection con)
    {
        Preconditions.checkState( ch.getHandle().eventLoop().inEventLoop(), "not in event loop" );
        if ( isQueuingConfigPackets() )
        {
            receivedConfigFinish = true;
        } else
        {
            finishConfigPhase( con );
        }
    }

    /*
     * Sets up a config queue, so config packets can be queued and sent after PlayerConfigurationEvent.
     */
    public void queueConfigPackets()
    {
        Preconditions.checkState( ch.getHandle().eventLoop().inEventLoop(), "not in event loop" );
        Preconditions.checkState( configQueue == null, "already queueing config packets" );
        configQueue = new ArrayDeque<>();

        // these packets have to be released when the channel closes
        // otherwise we would have a big memory leak
        ch.getHandle().closeFuture().addListener( (ChannelFutureListener) channelFuture -> releaseCachedPacketWrappers( null ) );
    }


    /*
     * Releases cached packet wrappers, either by sending them to the given player,
     * or by releasing them directly if the player is null.
     */
    private void releaseCachedPacketWrappers(UserConnection userConnection)
    {
        Preconditions.checkState( ch.getHandle().eventLoop().inEventLoop(), "not in event loop" );
        if ( configQueue != null )
        {
            configQueue.forEach( userConnection != null ? userConnection::sendPacket : PacketWrapper::trySingleRelease );
            configQueue.clear();
            configQueue = null;
        }
    }

    /*
     * Queues a config packet to be sent after PlayerConfigurationEvent.
     */
    public void queueConfigPacket(PacketWrapper packetWrapper)
    {
        Preconditions.checkState( ch.getHandle().eventLoop().inEventLoop(), "not in event loop" );
        Preconditions.checkNotNull( configQueue, "not queueing config packets" );
        Preconditions.checkNotNull( packetWrapper, "packetWrapper can not be null" );
        Preconditions.checkState( configQueue.size() <= 1024, "too many queued config packets" );
        Preconditions.checkState( configQueue.add( packetWrapper ), "could not add packetWrapper into configQueue" );
    }

    /*
     * Releases all queued config packets to the given player.
     * Note: if the player is not on this server anymore, the packets will be discarded.
     */
    public void releaseConfigPackets(UserConnection player)
    {
        Preconditions.checkState( ch.getHandle().eventLoop().inEventLoop(), "not in event loop" );
        Preconditions.checkNotNull( configQueue, "not holding back config packets" );
        Preconditions.checkNotNull( player, "player can not be null" );

        if ( !player.isConnected() || player.getServer() != this )
        {
            releaseCachedPacketWrappers( null );
            return;
        }

        releaseCachedPacketWrappers( player );

        if ( receivedConfigFinish )
        {
            finishConfigPhase( player );
        }
    }

    public void finishConfigPhase(UserConnection player)
    {
        Preconditions.checkState( ch.getHandle().eventLoop().inEventLoop(), "not in event loop" );
        player.unsafe().sendPacket( new FinishConfiguration() );
        // send queued packets as early as possible
        player.sendQueuedPackets();
        receivedConfigFinish = false;
    }

    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }

        @Override
        public void sendPacketQueued(DefinedPacket packet)
        {
            if ( ch.getEncodeVersion() >= ProtocolConstants.MINECRAFT_1_20_2 )
            {
                ServerConnection.this.sendPacketQueued( packet );
            } else
            {
                sendPacket( packet );
            }
        }
    };

    public void sendPacketQueued(DefinedPacket packet)
    {
        ch.scheduleIfNecessary( () ->
        {
            if ( ch.isClosed() )
            {
                return;
            }
            Protocol encodeProtocol = ch.getEncodeProtocol();
            if ( !encodeProtocol.TO_SERVER.hasPacket( packet.getClass(), ch.getEncodeVersion() ) )
            {
                // we should limit this so bad api usage won't oom the server.
                Preconditions.checkState( packetQueue.size() <= 4096, "too many queued packets" );
                packetQueue.add( packet );
            } else
            {
                unsafe().sendPacket( packet );
            }
        } );
    }

    public void sendQueuedPackets()
    {
        ch.scheduleIfNecessary( () ->
        {
            if ( ch.isClosed() )
            {
                return;
            }
            DefinedPacket packet;
            while ( ( packet = packetQueue.poll() ) != null )
            {
                unsafe().sendPacket( packet );
            }
        } );
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        sendPacketQueued( new PluginMessage( channel, data, forgeServer ) );
    }

    @Override
    public void disconnect(String reason)
    {
        disconnect();
    }

    @Override
    public void disconnect(BaseComponent... reason)
    {
        Preconditions.checkArgument( reason.length == 0, "Server cannot have disconnect reason" );

        isObsolete = true;
        ch.close();
    }

    @Override
    public void disconnect(BaseComponent reason)
    {
        disconnect();
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) getSocketAddress();
    }

    @Override
    public SocketAddress getSocketAddress()
    {
        return getInfo().getAddress();
    }

    @Override
    public boolean isConnected()
    {
        return !ch.isClosed();
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }

    @Data
    public static class KeepAliveData
    {

        private final long id;
        private final long time;
    }
}
