package net.md_5.bungee;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Queue;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.netty.ChannelBootstrapper;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketHandler;
import net.md_5.bungee.packet.PacketStream;

public class ServerConnector extends PacketHandler
{

    private final PacketStream stream;
    private Packet1Login loginPacket;
    private State thisState = State.ENCRYPT_REQUEST;

    public ServerConnector(PacketStream stream)
    {
        this.stream = stream;
    }

    private enum State
    {

        ENCRYPT_REQUEST, LOGIN, FINISHED;
    }

    @Override
    public void handle(Packet1Login login) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not exepcting LOGIN" );
        loginPacket = login;

        ServerConnection server = new ServerConnection( socket, info, stream, connector.loginPacket );
        ServerConnectedEvent event = new ServerConnectedEvent( user, server );
        ProxyServer.getInstance().getPluginManager().callEvent( event );

        stream.write( BungeeCord.getInstance().registerChannels() );

        Queue<DefinedPacket> packetQueue = ( (BungeeServerInfo) info ).getPacketQueue();
        while ( !packetQueue.isEmpty() )
        {
            stream.write( packetQueue.poll() );
        }
        thisState = State.FINISHED;
    }

    @Override
    public void handle(PacketFDEncryptionRequest encryptRequest) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT_REQUEST, "Not expecting ENCRYPT_REQUEST" );
        thisState = State.LOGIN;
    }

    @Override
    public void handle(PacketFFKick kick) throws Exception
    {
        throw new KickException( kick.message );
    }

    public static void connect(final UserConnection user, final ServerInfo info, final boolean retry)
    {
        new Bootstrap()
                .channel( NioSocketChannel.class )
                .group( BungeeCord.getInstance().eventLoops )
                .handler( ChannelBootstrapper.CLIENT )
                .remoteAddress( info.getAddress() )
                .connect().addListener( new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( future.isSuccess() )
                {
                    future.channel().write( user.handshake );
                    future.channel().write( PacketCDClientStatus.CLIENT_LOGIN );
                } else
                {
                    future.channel().close();
                    ServerInfo def = ProxyServer.getInstance().getServers().get( user.getPendingConnection().getListener().getDefaultServer() );
                    if ( retry && !info.equals( def ) )
                    {
                        user.sendMessage( ChatColor.RED + "Could not connect to target server, you have been moved to the default server" );
                        connect( user, def, false );
                    }
                }
            }
        } ).channel();
    }
}
