package net.md_5.bungee.connection;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.DataInput;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EntityMap;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.score.Objective;
import net.md_5.bungee.api.score.Position;
import net.md_5.bungee.api.score.Score;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Kick;

@RequiredArgsConstructor
public class DownstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;
    private final ServerConnection server;

    @Override
    public void connected( ChannelWrapper channel ) throws Exception {
        if( ! con.isActive() )
            server.disconnect( "End of Stream" );
        con.sendMessage( ChatColor.GREEN + "Welcome to " + ChatColor.RED + server.getInfo().getName() );
    }
    
    public void tryRescue( String kickMessage ) {
        ServerInfo def = bungee.getServerInfo( con.getPendingConnection().getListener().getFallbackServer() );
        if ( ! server.getInfo().getName().equalsIgnoreCase( BungeeCord.jailServerName ) )
        {
            server.setObsolete( true );
            con.connectNow( def );
            //con.sendMessage( bungee.getTranslation( "server_went_down" ) );
        } else
        {
            con.disconnect( kickMessage );
        }
    }
    
    @Override
    public void exception(Throwable t) throws Exception
    {
        tryRescue( Util.exception( t ) );
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        // We lost connection to the server
        server.getInfo().removePlayer( con );
        if ( bungee.getReconnectHandler() != null )
        {
            bungee.getReconnectHandler().setServer( con );
        }

        if ( !server.isObsolete() && con.isActive() )
        {
            tryRescue( bungee.getTranslation( "lost_connection" ) );
        }

        ServerDisconnectEvent serverDisconnectEvent = new ServerDisconnectEvent( con, server.getInfo() );
        bungee.getPluginManager().callEvent( serverDisconnectEvent );
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception
    {
        if ( !server.isObsolete() )
        {
            EntityMap.rewrite( packet.buf, con.getServerEntityId(), con.getClientEntityId() );
            con.sendPacket( packet );
        }
    }

    @Override
    public void handle(KeepAlive alive) throws Exception
    {
        con.setSentPingId( alive.getRandomId() );
        con.setSentPingTime( System.currentTimeMillis() );
    }

    @Override
    public void handle(PlayerListItem playerList) throws Exception
    {

        if ( !con.getTabList().onListUpdate( playerList.getUsername(), playerList.isOnline(), playerList.getPing() ) )
        {
            throw new CancelSendSignal();
        }
    }

    @Override
    public void handle(ScoreboardObjective objective) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        switch ( objective.getAction() )
        {
            case 0:
                serverScoreboard.addObjective( new Objective( objective.getName(), objective.getText() ) );
                break;
            case 1:
                serverScoreboard.removeObjective( objective.getName() );
                break;
        }
    }

    @Override
    public void handle(ScoreboardScore score) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        switch ( score.getAction() )
        {
            case 0:
                Score s = new Score( score.getItemName(), score.getScoreName(), score.getValue() );
                serverScoreboard.removeScore( score.getItemName() );
                serverScoreboard.addScore( s );
                break;
            case 1:
                serverScoreboard.removeScore( score.getItemName() );
                break;
        }
    }

    @Override
    public void handle(ScoreboardDisplay displayScoreboard) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        serverScoreboard.setName( displayScoreboard.getName() );
        serverScoreboard.setPosition( Position.values()[displayScoreboard.getPosition()] );
    }

    @Override
    public void handle(net.md_5.bungee.protocol.packet.Team team) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        // Remove team and move on
        if ( team.getMode() == 1 )
        {
            serverScoreboard.removeTeam( team.getName() );
            return;
        }

        // Create or get old team
        Team t;
        if ( team.getMode() == 0 )
        {
            t = new Team( team.getName() );
            serverScoreboard.addTeam( t );
        } else
        {
            t = serverScoreboard.getTeam( team.getName() );
        }

        if ( t != null )
        {
            if ( team.getMode() == 0 || team.getMode() == 2 )
            {
                t.setDisplayName( team.getDisplayName() );
                t.setPrefix( team.getPrefix() );
                t.setSuffix( team.getSuffix() );
                t.setFriendlyFire( team.isFriendlyFire() );
            }
            if ( team.getPlayers() != null )
            {
                for ( String s : team.getPlayers() )
                {
                    if ( team.getMode() == 0 || team.getMode() == 3 )
                    {
                        t.addPlayer( s );
                    } else
                    {
                        t.removePlayer( s );
                    }
                }
            }
        }
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        DataInput in = pluginMessage.getStream();
        PluginMessageEvent event = new PluginMessageEvent( con.getServer(), con, pluginMessage.getTag(), pluginMessage.getData().clone() );

        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            throw new CancelSendSignal();
        }

        if ( pluginMessage.getTag().equals( "BungeeCord" ) )
        {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            String subChannel = in.readUTF();

            if ( subChannel.equals( "Forward" ) )
            {
                // Read data from server
                String target = in.readUTF();
                String channel = in.readUTF();
                short len = in.readShort();
                byte[] data = new byte[ len ];
                in.readFully( data );

                // Prepare new data to send
                out.writeUTF( channel );
                out.writeShort( data.length );
                out.write( data );
                byte[] payload = out.toByteArray();

                // Null out stream, important as we don't want to send to ourselves
                out = null;

                if ( target.equals( "ALL" ) )
                {
                    for ( ServerInfo server : bungee.getServers().values() )
                    {
                        if ( server != con.getServer().getInfo() )
                        {
                            server.sendData( "BungeeCord", payload );
                        }
                    }
                } else
                {
                    ServerInfo server = bungee.getServerInfo( target );
                    if ( server != null )
                    {
                        server.sendData( "BungeeCord", payload );
                    }
                }
            }
            if ( subChannel.equals( "Connect" ) )
            {
                ServerInfo server = bungee.getServerInfo( in.readUTF() );
                if ( server != null )
                {
                    con.connect( server );
                }
            }
            if ( subChannel.equals( "ConnectOther" ) )
            {
                ProxiedPlayer player = bungee.getPlayer( in.readUTF() );
                if ( player != null )
                {
                    ServerInfo server = bungee.getServerInfo( in.readUTF() );
                    if ( server != null )
                    {
                        player.connect( server );
                    }
                }
            }
            if ( subChannel.equals( "IP" ) )
            {
                out.writeUTF( "IP" );
                out.writeUTF( con.getAddress().getHostString() );
                out.writeInt( con.getAddress().getPort() );
            }
            if ( subChannel.equals( "PlayerCount" ) )
            {
                String target = in.readUTF();
                out.writeUTF( "PlayerCount" );
                if ( target.equals( "ALL" ) )
                {
                    out.writeUTF( "ALL" );
                    out.writeInt( bungee.getOnlineCount() );
                } else
                {
                    ServerInfo server = bungee.getServerInfo( target );
                    if ( server != null )
                    {
                        out.writeUTF( server.getName() );
                        out.writeInt( server.getPlayers().size() );
                    }
                }
            }
            if ( subChannel.equals( "PlayerList" ) )
            {
                String target = in.readUTF();
                out.writeUTF( "PlayerList" );
                if ( target.equals( "ALL" ) )
                {
                    out.writeUTF( "ALL" );
                    out.writeUTF( Util.csv( bungee.getPlayers() ) );
                } else
                {
                    ServerInfo server = bungee.getServerInfo( target );
                    if ( server != null )
                    {
                        out.writeUTF( server.getName() );
                        out.writeUTF( Util.csv( server.getPlayers() ) );
                    }
                }
            }
            if ( subChannel.equals( "GetServers" ) )
            {
                out.writeUTF( "GetServers" );
                out.writeUTF( Util.csv( bungee.getServers().keySet() ) );
            }
            if ( subChannel.equals( "Message" ) )
            {
                ProxiedPlayer target = bungee.getPlayer( in.readUTF() );
                if ( target != null )
                {
                    target.sendMessage( in.readUTF() );
                }
            }
            if ( subChannel.equals( "GetServer" ) )
            {
                out.writeUTF( "GetServer" );
                out.writeUTF( server.getInfo().getName() );
            }

            // Check we haven't set out to null, and we have written data, if so reply back back along the BungeeCord channel
            if ( out != null )
            {
                byte[] b = out.toByteArray();
                if ( b.length != 0 )
                {
                    con.getServer().sendData( "BungeeCord", b );
                }
            }
        }
    }

    @Override
    public void handle(Kick kick) throws Exception
    {
        ServerInfo def = bungee.getServerInfo( con.getPendingConnection().getListener().getFallbackServer() );
        /*
        if ( Objects.equals( server.getInfo(), def ) )
        {
            def = null;
        }*/
        ServerKickEvent origEvt = new ServerKickEvent( con, kick.getMessage(), def, ServerKickEvent.State.CONNECTED );
        
        if( ! server.getInfo().getName().equalsIgnoreCase( BungeeCord.jailServerName ) && ( kick.getMessage().contains( "Server" ) || kick.getMessage().contains( "closed" ) || kick.getMessage().contains( "white-listed" ) ) )
            origEvt.setCancelled( true );
        ServerKickEvent event = bungee.getPluginManager().callEvent( origEvt );
        if ( event.isCancelled() && event.getCancelServer() != null )
        {
            server.setObsolete( true );
            con.connectNow( event.getCancelServer() );
        } else
        {
            con.disconnect0( event.getKickReason() ); // TODO: Json concat util method // TODO: Prefix our own stuff.
            server.setObsolete( true ); // TODO: Is this still needed?
        }
        throw new CancelSendSignal();
    }

    @Override
    public String toString()
    {
        return "[" + con.getName() + "] <-> DownstreamBridge <-> [" + server.getInfo().getName() + "]";
    }
}
