package net.md_5.bungee;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.netty.ChannelBootstrapper;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.Packet9Respawn;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketHandler;

@RequiredArgsConstructor
public class ServerConnector extends PacketHandler
{

    private final ProxyServer bungee;
    private final Channel ch;
    private final UserConnection user;
    private final ServerInfo target;
    private State thisState = State.ENCRYPT_REQUEST;

    private enum State
    {

        ENCRYPT_REQUEST, LOGIN, FINISHED;
    }

    @Override
    public void handle(Packet1Login login) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not exepcting LOGIN" );

        ServerConnection server = new ServerConnection( ch, target, login );
        ServerConnectedEvent event = new ServerConnectedEvent( user, server );
        bungee.getPluginManager().callEvent( event );

        ch.write( BungeeCord.getInstance().registerChannels() );

        Queue<DefinedPacket> packetQueue = ( (BungeeServerInfo) target ).getPacketQueue();
        while ( !packetQueue.isEmpty() )
        {
            ch.write( packetQueue.poll() );
        }

        synchronized ( user.getSwitchMutex() )
        {
            if ( user.getServer() == null )
            {
                BungeeCord.getInstance().connections.put( user.getName(), user );
                bungee.getTabListHandler().onConnect( user );
                // Once again, first connection
                user.clientEntityId = login.entityId;
                user.serverEntityId = login.entityId;
                // Set tab list size
                Packet1Login modLogin = new Packet1Login(
                        login.entityId,
                        login.levelType,
                        login.gameMode,
                        (byte) login.dimension,
                        login.difficulty,
                        login.unused,
                        (byte) user.getPendingConnection().getListener().getTabListSize() );
                ch.write( modLogin );
                ch.write( BungeeCord.getInstance().registerChannels() );
            } else
            {
                bungee.getTabListHandler().onServerChange( user );
                user.sendPacket( Packet9Respawn.DIM1_SWITCH );
                user.sendPacket( Packet9Respawn.DIM2_SWITCH );

                user.serverEntityId = login.entityId;
                ch.write( new Packet9Respawn( login.dimension, login.difficulty, login.gameMode, (short) 256, login.levelType ) );

                // Add to new server
                target.addPlayer( user );
                // Remove from old servers
                user.getServer().disconnect( "Quitting" );
                user.getServer().getInfo().removePlayer( user );
            }
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

    public static void connect(final UserConnection user, ServerInfo info, final boolean retry)
    {
        ServerConnectEvent event = new ServerConnectEvent( user, info );
        ProxyServer.getInstance().getPluginManager().callEvent( event );
        final ServerInfo target = event.getTarget(); // Update in case the event changed target

        ChannelBootstrapper.CLIENT.connectClient( info.getAddress() ).addListener( new ChannelFutureListener()
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
                    if ( retry && !target.equals( def ) )
                    {
                        user.sendMessage( ChatColor.RED + "Could not connect to target server, you have been moved to the default server" );
                        connect( user, def, false );
                    }
                }
            }
        } ).channel();
    }
}
