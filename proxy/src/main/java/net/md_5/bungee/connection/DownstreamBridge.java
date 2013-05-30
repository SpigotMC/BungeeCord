package net.md_5.bungee.connection;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.EntityMap;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.scoreboard.Objective;
import net.md_5.bungee.api.scoreboard.Position;
import net.md_5.bungee.api.scoreboard.Score;
import net.md_5.bungee.api.scoreboard.Scoreboard;
import net.md_5.bungee.api.scoreboard.Team;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.Packet0KeepAlive;
import net.md_5.bungee.protocol.packet.Packet3Chat;
import net.md_5.bungee.protocol.packet.PacketC9PlayerListItem;
import net.md_5.bungee.protocol.packet.PacketCEScoreboardObjective;
import net.md_5.bungee.protocol.packet.PacketCFScoreboardScore;
import net.md_5.bungee.protocol.packet.PacketD0DisplayScoreboard;
import net.md_5.bungee.protocol.packet.PacketD1Team;
import net.md_5.bungee.protocol.packet.PacketFAPluginMessage;
import net.md_5.bungee.protocol.packet.PacketFFKick;
import net.md_5.bungee.protocol.packet.PacketHandler;

@RequiredArgsConstructor
public class DownstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;
    private final ServerConnection server;

    @Override
    public void exception(Throwable t) throws Exception
    {
        ServerInfo def = bungee.getServerInfo( con.getPendingConnection().getListener().getFallbackServer() );
        if ( server.getInfo() != def )
        {
            con.connectNow( def );
            con.sendMessage( ChatColor.RED + "The server you were previously on went down, you have been connected to the lobby" );
        } else
        {
            con.disconnect( Util.exception( t ) );
        }
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        // We lost connection to the server
        server.getInfo().removePlayer( con );
        bungee.getReconnectHandler().setServer( con );

        if ( !server.isObsolete() )
        {
            con.disconnect( bungee.getTranslation( "lost_connection" ) );
        }
    }

    @Override
    public void handle(byte[] buf) throws Exception
    {
        EntityMap.rewrite( buf, con.getServerEntityId(), con.getClientEntityId() );
        con.sendPacket( buf );
    }

    @Override
    public void handle(Packet0KeepAlive alive) throws Exception
    {
        con.setSentPingId( alive.id );
        con.setSentPingTime( System.currentTimeMillis() );
    }

    @Override
    public void handle(PacketC9PlayerListItem playerList) throws Exception
    {

        if ( !bungee.getTabListHandler().onListUpdate( con, playerList.username, playerList.online, playerList.ping ) )
        {
            throw new CancelSendSignal();
        }
    }

    @Override
    public void handle(PacketCEScoreboardObjective objective) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        switch ( objective.action )
        {
            case 0:
                serverScoreboard.addObjective( new Objective( objective.name, objective.text ) );
                break;
            case 1:
                serverScoreboard.removeObjective( objective.name );
                break;
        }
    }

    @Override
    public void handle(PacketCFScoreboardScore score) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        switch ( score.action )
        {
            case 0:
                Score s = new Score( score.itemName, score.scoreName, score.value );
                serverScoreboard.removeScore( score.itemName );
                serverScoreboard.addScore( s );
                break;
            case 1:
                serverScoreboard.removeScore( score.itemName );
                break;
        }
    }

    @Override
    public void handle(PacketD0DisplayScoreboard displayScoreboard) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        serverScoreboard.setName( displayScoreboard.name );
        serverScoreboard.setPosition( Position.values()[displayScoreboard.position] );
    }

    @Override
    public void handle(PacketD1Team team) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        // Remove team and move on
        if ( team.mode == 1 )
        {
            serverScoreboard.removeTeam( team.name );
            return;
        }

        // Create or get old team
        Team t;
        if ( team.mode == 0 )
        {
            t = new Team( team.name );
            serverScoreboard.addTeam( t );
        } else
        {
            t = serverScoreboard.getTeam( team.name );
        }

        if ( t != null )
        {
            if ( team.mode == 0 || team.mode == 2 )
            {
                t.setDisplayName( team.displayName );
                t.setPrefix( team.prefix );
                t.setSuffix( team.suffix );
                t.setFriendlyMode( team.friendlyFire );
            }
            if ( team.players != null )
            {
                for ( String s : team.players )
                {
                    if ( team.mode == 0 || team.mode == 3 )
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
    public void handle(PacketFAPluginMessage pluginMessage) throws Exception
    {
        ByteArrayDataInput in = ByteStreams.newDataInput( pluginMessage.data );
        PluginMessageEvent event = new PluginMessageEvent( con.getServer(), con, pluginMessage.tag, pluginMessage.data.clone() );

        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            throw new CancelSendSignal();
        }

        if ( pluginMessage.tag.equals( "MC|TPack" ) && con.getPendingConnection().getListener().getTexturePack() != null )
        {
            throw new CancelSendSignal();
        }

        if ( pluginMessage.tag.equals( "BungeeCord" ) )
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
    public void handle(PacketFFKick kick) throws Exception
    {
        ServerInfo def = bungee.getServerInfo( con.getPendingConnection().getListener().getFallbackServer() );
        if ( Objects.equals( server.getInfo(), def ) )
        {
            def = null;
        }
        ServerKickEvent event = bungee.getPluginManager().callEvent( new ServerKickEvent( con, kick.message, def ) );
        if ( event.isCancelled() && event.getCancelServer() != null )
        {
            con.connectNow( event.getCancelServer() );
        } else
        {
            con.disconnect( bungee.getTranslation( "server_kick" ) + event.getKickReason() );
        }
        server.setObsolete( true );
        throw new CancelSendSignal();
    }

    @Override
    public String toString()
    {
        return "[" + con.getName() + "] <-> DownstreamBridge <-> [" + server.getInfo().getName() + "]";
    }
}
