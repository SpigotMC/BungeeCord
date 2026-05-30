package net.md_5.bungee;

import com.google.common.base.Preconditions;
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
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.LoginAcknowledged;
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
    @Getter
    @Setter
    private ConfigurationStateTracker configurationStateTracker = new ConfigurationStateTracker();

    public static class ConfigurationStateTracker
    {
        @Getter
        @Setter
        private boolean firstLogin;
        private int awaitingKnownPacks;
        @Getter
        @Setter
        private boolean awaitingFinish;

        public boolean isAwaitingKnownPacks()
        {
            return awaitingKnownPacks > 0;
        }

        public void incrementAwaitingKnownPacks()
        {
            ++awaitingKnownPacks;
        }

        public void decrementAwaitingKnownPacks()
        {
            Preconditions.checkState( --awaitingKnownPacks >= 0, "awaitingKnownPacks must be >= 0" );
        }
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

    public void completeLogin(UserConnection con)
    {
        ch.setDecodeProtocol( Protocol.CONFIGURATION );
        ch.write( new LoginAcknowledged() );
        ch.setEncodeProtocol( Protocol.CONFIGURATION );

        // send the registered plugin channel as soon as the server is in config state
        ch.write( BungeeCord.getInstance().registerChannels( con.getPendingConnection().getVersion() ) );
        if ( con.getSettings() != null )
        {
            ch.write( con.getSettings() );
        }
        if ( con.getPendingConnection().getBrandMessage() != null )
        {
            ch.write( con.getPendingConnection().getBrandMessage() );
        }
        sendQueuedPackets();
    }
}
