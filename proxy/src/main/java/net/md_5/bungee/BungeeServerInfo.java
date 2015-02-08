package net.md_5.bungee;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.connection.PingHandler;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;

@RequiredArgsConstructor
@ToString(of =
{
    "name", "address", "restricted"
})
public class BungeeServerInfo implements ServerInfo
{

    @Getter
    private final String name;
    @Getter
    private final InetSocketAddress address;
    private final Collection<ProxiedPlayer> players = new ArrayList<>();
    @Getter
    private final String motd;
    @Getter
    private final boolean restricted;
    @Getter
    private final Queue<DefinedPacket> packetQueue = new LinkedList<>();

    @Synchronized("players")
    public void addPlayer(ProxiedPlayer player)
    {
        players.add( player );
    }

    @Synchronized("players")
    public void removePlayer(ProxiedPlayer player)
    {
        players.remove( player );
    }

    @Synchronized("players")
    @Override
    public Collection<ProxiedPlayer> getPlayers()
    {
        return Collections.unmodifiableCollection( new HashSet( players ) );
    }

    @Override
    public boolean canAccess(CommandSender player)
    {
        Preconditions.checkNotNull( player, "player" );
        return !restricted || player.hasPermission( "bungeecord.server." + name );
    }

    @Override
    public boolean equals(Object obj)
    {
        return ( obj instanceof ServerInfo ) && Objects.equal( getAddress(), ( (ServerInfo) obj ).getAddress() );
    }

    @Override
    public int hashCode()
    {
        return address.hashCode();
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        sendData( channel, data, true );
    }

    // TODO: Don't like this method
    @Override
    public boolean sendData(String channel, byte[] data, boolean queue)
    {
        Preconditions.checkNotNull( channel, "channel" );
        Preconditions.checkNotNull( data, "data" );

        synchronized ( packetQueue )
        {
            Server server = ( players.isEmpty() ) ? null : players.iterator().next().getServer();
            if ( server != null )
            {
                server.sendData( channel, data );
                return true;
            } else if ( queue )
            {
                packetQueue.add( new PluginMessage( channel, data, false ) );
            }
            return false;
        }
    }

    @Override
    public void ping(final Callback<ServerPing> callback)
    {
        ping( callback, ProxyServer.getInstance().getProtocolVersion() );
    }

    public void ping(final Callback<ServerPing> callback, final int protocolVersion)
    {
        Preconditions.checkNotNull( callback, "callback" );

        ChannelFutureListener listener = new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( future.isSuccess() )
                {
                    future.channel().pipeline().get( HandlerBoss.class ).setHandler( new PingHandler( BungeeServerInfo.this, callback, protocolVersion ) );
                } else
                {
                    callback.done( null, future.cause() );
                }
            }
        };
        new Bootstrap()
                .channel( PipelineUtils.getChannel() )
                .group( BungeeCord.getInstance().eventLoops )
                .handler( PipelineUtils.BASE )
                .option( ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000 ) // TODO: Configurable
                .remoteAddress( getAddress() )
                .connect()
                .addListener( listener );
    }
}
