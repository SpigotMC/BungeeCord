package net.md_5.bungee;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.Objects;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.score.Objective;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.MinecraftOutput;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LoginSuccess;

@RequiredArgsConstructor
public class ServerConnector extends PacketHandler
{

    private final ProxyServer bungee;
    private ChannelWrapper ch;
    private final UserConnection user;
    private final BungeeServerInfo target;
    private State thisState = State.LOGIN_SUCCESS;

    private enum State
    {

        LOGIN_SUCCESS, ENCRYPT_RESPONSE, LOGIN, FINISHED;
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        String message = "Exception Connecting:" + Util.exception( t );
        if ( user.getServer() == null )
        {
            user.disconnect( message );
        } else
        {
            user.sendMessage( ChatColor.RED + message );
        }
    }

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.ch = channel;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF( "Login" );
        out.writeUTF( user.getAddress().getHostString() );
        out.writeInt( user.getAddress().getPort() );
        // channel.write( new PluginMessage( "BungeeCord", out.toByteArray() ) ); MOJANG

        channel.write( user.getPendingConnection().getHandshake() );

        channel.setProtocol( Protocol.LOGIN );
        channel.write( user.getPendingConnection().getLoginRequest() );
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        user.getPendingConnects().remove( target );
    }

    @Override
    public void handle(LoginSuccess loginSuccess) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN_SUCCESS, "Not exepcting LOGIN_SUCCESS" );
        ch.setProtocol( Protocol.GAME );
        thisState = State.LOGIN;

        throw new CancelSendSignal();
    }

    @Override
    public void handle(Login login) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not exepcting LOGIN" );

        ServerConnection server = new ServerConnection( ch, target );
        ServerConnectedEvent event = new ServerConnectedEvent( user, server );
        bungee.getPluginManager().callEvent( event );

        ch.write( BungeeCord.getInstance().registerChannels() );
        Queue<DefinedPacket> packetQueue = target.getPacketQueue();
        synchronized ( packetQueue )
        {
            while ( !packetQueue.isEmpty() )
            {
                ch.write( packetQueue.poll() );
            }
        }

        for ( PluginMessage message : user.getPendingConnection().getRegisterMessages() )
        {
            ch.write( message );
        }

        if ( user.getSettings() != null )
        {
            ch.write( user.getSettings() );
        }

        synchronized ( user.getSwitchMutex() )
        {
            if ( user.getServer() == null )
            {
                // Once again, first connection
                user.setClientEntityId( login.getEntityId() );
                user.setServerEntityId( login.getEntityId() );

                // Set tab list size, this sucks balls, TODO: what shall we do about packet mutability
                Login modLogin = new Login( login.getEntityId(), login.getLevelType(), login.getGameMode(), (byte) login.getDimension(), login.getDifficulty(), login.getUnused(),
                        (byte) user.getPendingConnection().getListener().getTabListSize() );

                user.unsafe().sendPacket( modLogin );

                MinecraftOutput out = new MinecraftOutput();
                out.writeStringUTF8WithoutLengthHeaderBecauseDinnerboneStuffedUpTheMCBrandPacket( ProxyServer.getInstance().getName() + " (" + ProxyServer.getInstance().getVersion() + ")" );
                user.unsafe().sendPacket( new PluginMessage( "MC|Brand", out.toArray() ) );
            } else
            {
                user.getTabList().onServerChange();

                Scoreboard serverScoreboard = user.getServerSentScoreboard();
                for ( Objective objective : serverScoreboard.getObjectives() )
                {
                    user.unsafe().sendPacket( new ScoreboardObjective( objective.getName(), objective.getValue(), (byte) 1 ) );
                }
                for ( Team team : serverScoreboard.getTeams() )
                {
                    user.unsafe().sendPacket( new net.md_5.bungee.protocol.packet.Team( team.getName() ) );
                }
                serverScoreboard.clear();

                user.sendDimensionSwitch();

                user.setServerEntityId( login.getEntityId() );
                user.unsafe().sendPacket( new Respawn( login.getDimension(), login.getDifficulty(), login.getGameMode(), (short) 256, login.getLevelType() ) );

                // Remove from old servers
                user.getServer().setObsolete( true );
                user.getServer().disconnect( "Quitting" );
            }

            // TODO: Fix this?
            if ( !user.isActive() )
            {
                server.disconnect( "Quitting" );
                // Silly server admins see stack trace and die
                bungee.getLogger().warning( "No client connected for pending server!" );
                return;
            }

            // Add to new server
            // TODO: Move this to the connected() method of DownstreamBridge
            target.addPlayer( user );
            user.getPendingConnects().remove( target );

            user.setServer( server );
            ch.getHandle().pipeline().get( HandlerBoss.class ).setHandler( new DownstreamBridge( bungee, user, server ) );
        }

        bungee.getPluginManager().callEvent( new ServerSwitchEvent( user ) );

        thisState = State.FINISHED;

        throw new CancelSendSignal();
    }

    @Override
    public void handle(Kick kick) throws Exception
    {
        ServerInfo def = bungee.getServerInfo( user.getPendingConnection().getListener().getFallbackServer() );
        if ( Objects.equals( target, def ) )
        {
            def = null;
        }
        ServerKickEvent event = bungee.getPluginManager().callEvent( new ServerKickEvent( user, kick.getMessage(), def, ServerKickEvent.State.CONNECTING ) );
        if ( event.isCancelled() && event.getCancelServer() != null )
        {
            user.connect( event.getCancelServer() );
            return;
        }

        String message = bungee.getTranslation( "connect_kick" ) + target.getName() + ": " + event.getKickReason();
        if ( user.getServer() == null )
        {
            user.disconnect( message );
        } else
        {
            user.sendMessage( message );
        }
    }

    @Override
    public String toString()
    {
        return "[" + user.getName() + "] <-> ServerConnector [" + target.getName() + "]";
    }
}
