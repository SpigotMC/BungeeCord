package net.md_5.bungee.connection;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.unix.DomainSocketAddress;
import java.io.DataInput;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.ServerConnection.KeepAliveData;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.score.Objective;
import net.md_5.bungee.api.score.Position;
import net.md_5.bungee.api.score.Score;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.BossBar;
import net.md_5.bungee.protocol.packet.Commands;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.SetCompression;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;
import net.md_5.bungee.tab.TabList;

@RequiredArgsConstructor
public class DownstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;
    private final ServerConnection server;

    @Override
    public void exception(Throwable t) throws Exception
    {
        if ( server.isObsolete() )
        {
            // do not perform any actions if the user has already moved
            return;
        }

        ServerInfo def = con.updateAndGetNextServer( server.getInfo() );
        if ( def != null )
        {
            server.setObsolete( true );
            con.connectNow( def, ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT );
            con.sendMessage( bungee.getTranslation( "server_went_down" ) );
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
        if ( bungee.getReconnectHandler() != null )
        {
            bungee.getReconnectHandler().setServer( con );
        }

        if ( !server.isObsolete() )
        {
            con.disconnect( bungee.getTranslation( "lost_connection" ) );
        }

        ServerDisconnectEvent serverDisconnectEvent = new ServerDisconnectEvent( con, server.getInfo() );
        bungee.getPluginManager().callEvent( serverDisconnectEvent );
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleFully(PacketWrapper<?> packet) throws Exception
    {
        if ( server.isObsolete() )
        {
            return;
        }
        if ( packet.packet != null )
        {
            packet.packet.callHandler( this, packet );
        } else
        {
            handleGeneral( packet );
        }
    }

    @Override
    public void handleGeneral(PacketWrapper<?> packet)
    {
        con.getEntityRewrite().rewriteClientbound( packet.buf, con.getServerEntityId(), con.getClientEntityId(), con.getPendingConnection().getVersion() );
        con.sendPacket( packet );
    }

    @Override
    public void handleGeneralNoEntity(PacketWrapper<?> packet)
    {
        con.sendPacket( packet );
    }

    @Override
    public void handleKeepAlive(PacketWrapper<KeepAlive> packet) throws Exception
    {
        int timeout = bungee.getConfig().getTimeout();
        if ( timeout <= 0 || server.getKeepAlives().size() < timeout / 50 ) // Some people disable timeout, otherwise allow a theoretical maximum of 1 keepalive per tick
        {
            server.getKeepAlives().add( new KeepAliveData( packet.packet.getRandomId(), System.currentTimeMillis() ) );
        }
        handleGeneralNoEntity( packet );
    }

    @Override
    public void handlePlayerListItem(PacketWrapper<PlayerListItem> packet) throws Exception
    {
        con.getTabListHandler().onUpdate( TabList.rewrite( packet.packet ) );
        // onUpdate sends modified packet, so we do nothing here
    }

    @Override
    public void handleScoreboardObjective(PacketWrapper<ScoreboardObjective> packet) throws Exception
    {
        final ScoreboardObjective objective = packet.packet;
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        switch ( objective.getAction() )
        {
            case 0:
                serverScoreboard.addObjective( new Objective( objective.getName(), objective.getValue(), objective.getType().toString() ) );
                break;
            case 1:
                serverScoreboard.removeObjective( objective.getName() );
                break;
            case 2:
                Objective oldObjective = serverScoreboard.getObjective( objective.getName() );
                if ( oldObjective != null )
                {
                    oldObjective.setValue( objective.getValue() );
                    oldObjective.setType( objective.getType().toString() );
                }
                break;
            default:
                throw new IllegalArgumentException( "Unknown objective action: " + objective.getAction() );
        }
        handleGeneralNoEntity( packet );
    }

    @Override
    public void handleScoreboardScore(PacketWrapper<ScoreboardScore> packet) throws Exception
    {
        final ScoreboardScore score = packet.packet;
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
            default:
                throw new IllegalArgumentException( "Unknown scoreboard action: " + score.getAction() );
        }
        handleGeneralNoEntity( packet );
    }

    @Override
    public void handleScoreboardDisplay(PacketWrapper<ScoreboardDisplay> packet) throws Exception
    {
        final ScoreboardDisplay displayScoreboard = packet.packet;
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        serverScoreboard.setName( displayScoreboard.getName() );
        serverScoreboard.setPosition( Position.values()[ displayScoreboard.getPosition() ] );
        handleGeneralNoEntity( packet );
    }

    @Override
    public void handleTeam(PacketWrapper<net.md_5.bungee.protocol.packet.Team> packet) throws Exception
    {
        final net.md_5.bungee.protocol.packet.Team team = packet.packet;
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        // Remove team and move on
        final byte teamMode = team.getMode();
        if ( teamMode == 1 )
        {
            serverScoreboard.removeTeam( team.getName() );
            handleGeneralNoEntity( packet );
            return;
        }

        // Create or get old team
        Team t;
        if ( teamMode == 0 )
        {
            t = new Team( team.getName() );
            serverScoreboard.addTeam( t );
        } else
        {
            t = serverScoreboard.getTeam( team.getName() );
        }

        if ( t != null )
        {
            if ( teamMode == 0 || teamMode == 2 )
            {
                t.setDisplayName( team.getDisplayName() );
                t.setPrefix( team.getPrefix() );
                t.setSuffix( team.getSuffix() );
                t.setFriendlyFire( team.getFriendlyFire() );
                t.setNameTagVisibility( team.getNameTagVisibility() );
                t.setCollisionRule( team.getCollisionRule() );
                t.setColor( team.getColor() );
            }
            if ( team.getPlayers() != null )
            {
                for ( String s : team.getPlayers() )
                {
                    if ( teamMode == 0 || teamMode == 3 )
                    {
                        t.addPlayer( s );
                    } else if ( teamMode == 4 )
                    {
                        t.removePlayer( s );
                    }
                }
            }
        }
        handleGeneralNoEntity( packet );
    }

    @Override
    public void handlePluginMessage(PacketWrapper<PluginMessage> packet) throws Exception
    {
        final PluginMessage pluginMessage = packet.packet;
        DataInput in = pluginMessage.getStream();
        PluginMessageEvent event = new PluginMessageEvent( server, con, pluginMessage.getTag(), pluginMessage.getData().clone() );

        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            return;
        }

        if ( pluginMessage.getTag().equals( con.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_13 ? "minecraft:brand" : "MC|Brand" ) )
        {
            ByteBuf brand = Unpooled.wrappedBuffer( pluginMessage.getData() );
            String serverBrand = DefinedPacket.readString( brand );
            brand.release();

            Preconditions.checkState( !serverBrand.contains( bungee.getName() ), "Cannot connect proxy to itself!" );

            brand = ByteBufAllocator.DEFAULT.heapBuffer();
            DefinedPacket.writeString( bungee.getName() + " (" + bungee.getVersion() + ")" + " <- " + serverBrand, brand );
            pluginMessage.setData( DefinedPacket.toArray( brand ) );
            brand.release();
            // changes in the packet are ignored so we need to send it manually
            con.unsafe().sendPacket( pluginMessage );
            return;
        }

        if ( pluginMessage.getTag().equals( "BungeeCord" ) )
        {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            String subChannel = in.readUTF();

            if ( subChannel.equals( "ForwardToPlayer" ) )
            {
                ProxiedPlayer target = bungee.getPlayer( in.readUTF() );
                if ( target != null )
                {
                    // Read data from server
                    String channel = in.readUTF();
                    short len = in.readShort();
                    byte[] data = new byte[ len ];
                    in.readFully( data );

                    // Prepare new data to send
                    out.writeUTF( channel );
                    out.writeShort( data.length );
                    out.write( data );
                    byte[] payload = out.toByteArray();

                    target.getServer().sendData( "BungeeCord", payload );
                }

                // Null out stream, important as we don't want to send to ourselves
                out = null;
            }
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
                        if ( server != this.server.getInfo() )
                        {
                            server.sendData( "BungeeCord", payload );
                        }
                    }
                } else if ( target.equals( "ONLINE" ) )
                {
                    for ( ServerInfo server : bungee.getServers().values() )
                    {
                        if ( server != this.server.getInfo() )
                        {
                            server.sendData( "BungeeCord", payload, false );
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
                    con.connect( server, ServerConnectEvent.Reason.PLUGIN_MESSAGE );
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
                if ( con.getSocketAddress() instanceof InetSocketAddress )
                {
                    out.writeUTF( con.getAddress().getHostString() );
                    out.writeInt( con.getAddress().getPort() );
                } else
                {
                    out.writeUTF( "unix://" + ( (DomainSocketAddress) con.getSocketAddress() ).path() );
                    out.writeInt( 0 );
                }
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
                String target = in.readUTF();
                String message = in.readUTF();
                if ( target.equals( "ALL" ) )
                {
                    for ( ProxiedPlayer player : bungee.getPlayers() )
                    {
                        player.sendMessage( message );
                    }
                } else
                {
                    ProxiedPlayer player = bungee.getPlayer( target );
                    if ( player != null )
                    {
                        player.sendMessage( message );
                    }
                }
            }
            if ( subChannel.equals( "GetServer" ) )
            {
                out.writeUTF( "GetServer" );
                out.writeUTF( server.getInfo().getName() );
            }
            if ( subChannel.equals( "UUID" ) )
            {
                out.writeUTF( "UUID" );
                out.writeUTF( con.getUUID() );
            }
            if ( subChannel.equals( "UUIDOther" ) )
            {
                ProxiedPlayer player = bungee.getPlayer( in.readUTF() );
                if ( player != null )
                {
                    out.writeUTF( "UUIDOther" );
                    out.writeUTF( player.getName() );
                    out.writeUTF( player.getUUID() );
                }
            }
            if ( subChannel.equals( "ServerIP" ) )
            {
                ServerInfo info = bungee.getServerInfo( in.readUTF() );
                if ( info != null && !info.getAddress().isUnresolved() )
                {
                    out.writeUTF( "ServerIP" );
                    out.writeUTF( info.getName() );
                    out.writeUTF( info.getAddress().getAddress().getHostAddress() );
                    out.writeShort( info.getAddress().getPort() );
                }
            }
            if ( subChannel.equals( "KickPlayer" ) )
            {
                ProxiedPlayer player = bungee.getPlayer( in.readUTF() );
                if ( player != null )
                {
                    String kickReason = in.readUTF();
                    player.disconnect( new TextComponent( kickReason ) );
                }
            }

            // Check we haven't set out to null, and we have written data, if so reply back back along the BungeeCord channel
            if ( out != null )
            {
                byte[] b = out.toByteArray();
                if ( b.length != 0 )
                {
                    server.sendData( "BungeeCord", b );
                }
            }

            return;
        }
        handleGeneralNoEntity( packet );
    }

    @Override
    public void handleKick(PacketWrapper<Kick> packet) throws Exception
    {
        ServerInfo def = con.updateAndGetNextServer( server.getInfo() );
        ServerKickEvent event = bungee.getPluginManager().callEvent( new ServerKickEvent( con, server.getInfo(), ComponentSerializer.parse( packet.packet.getMessage() ), def, ServerKickEvent.State.CONNECTED ) );
        if ( event.isCancelled() && event.getCancelServer() != null )
        {
            con.connectNow( event.getCancelServer(), ServerConnectEvent.Reason.KICK_REDIRECT );
        } else
        {
            con.disconnect0( event.getKickReasonComponent() ); // TODO: Prefix our own stuff.
        }
        server.setObsolete( true );
    }

    @Override
    public void handleSetCompression(PacketWrapper<SetCompression> packet) throws Exception
    {
        server.getCh().setCompressionThreshold( packet.packet.getThreshold() );
        handleGeneralNoEntity( packet );
    }

    @Override
    public void handleTabCompleteResponse(PacketWrapper<TabCompleteResponse> packet) throws Exception
    {
        final TabCompleteResponse tabCompleteResponse = packet.packet;
        List<String> commands = tabCompleteResponse.getCommands();
        if ( commands == null )
        {
            commands = Lists.transform( tabCompleteResponse.getSuggestions().getList(), new Function<Suggestion, String>()
            {
                @Override
                public String apply(Suggestion input)
                {
                    return input.getText();
                }
            } );
        }

        TabCompleteResponseEvent tabCompleteResponseEvent = new TabCompleteResponseEvent( server, con, new ArrayList<>( commands ) );
        if ( !bungee.getPluginManager().callEvent( tabCompleteResponseEvent ).isCancelled() )
        {
            // Take action only if modified
            if ( !commands.equals( tabCompleteResponseEvent.getSuggestions() ) )
            {
                if ( tabCompleteResponse.getCommands() != null )
                {
                    // Classic style
                    tabCompleteResponse.setCommands( tabCompleteResponseEvent.getSuggestions() );
                } else
                {
                    // Brigadier style
                    final StringRange range = tabCompleteResponse.getSuggestions().getRange();
                    tabCompleteResponse.setSuggestions( new Suggestions( range, Lists.transform( tabCompleteResponseEvent.getSuggestions(), new Function<String, Suggestion>()
                    {
                        @Override
                        public Suggestion apply(String input)
                        {
                            return new Suggestion( range, input );
                        }
                    } ) ) );
                }
            }

            con.unsafe().sendPacket( tabCompleteResponse );
        }
        // Don't pipe through as we sent custom already or event cancelled
    }

    @Override
    public void handleBossBar(PacketWrapper<BossBar> packet) throws Exception
    {
        final BossBar bossBar = packet.packet;
        switch ( bossBar.getAction() )
        {
            // Handle add bossbar
            case 0:
                con.getSentBossBars().add( bossBar.getUuid() );
                break;
            // Handle remove bossbar
            case 1:
                con.getSentBossBars().remove( bossBar.getUuid() );
                break;
        }
        handleGeneralNoEntity( packet );
    }

    @Override
    public void handleRespawn(PacketWrapper<Respawn> packet) throws Exception
    {
        con.setDimension( packet.packet.getDimension() );
        handleGeneralNoEntity( packet );
    }

    @Override
    public void handleCommands(PacketWrapper<Commands> packet) throws Exception
    {
        boolean modified = false;
        final Commands commands = packet.packet;

        for ( Map.Entry<String, Command> command : bungee.getPluginManager().getCommands() )
        {
            if ( !bungee.getDisabledCommands().contains( command.getKey() ) && commands.getRoot().getChild( command.getKey() ) == null && command.getValue().hasPermission( con ) )
            {
                LiteralCommandNode dummy = LiteralArgumentBuilder.literal( command.getKey() )
                        .then( RequiredArgumentBuilder.argument( "args", StringArgumentType.greedyString() )
                                .suggests( Commands.SuggestionRegistry.ASK_SERVER ) )
                        .build();
                commands.getRoot().addChild( dummy );

                modified = true;
            }
        }

        if ( modified )
        {
            con.unsafe().sendPacket( commands );
        } else
        {
            handleGeneralNoEntity( packet );
        }
    }

    @Override
    public String toString()
    {
        return "[" + con.getName() + "] <-> DownstreamBridge <-> [" + server.getInfo().getName() + "]";
    }
}
