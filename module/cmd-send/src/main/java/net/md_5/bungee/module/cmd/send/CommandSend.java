package net.md_5.bungee.module.cmd.send;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandSend extends Command implements TabExecutor
{

    private static final int HOVER_NAMES_LIMIT = 100;

    protected static class SendCallback
    {

        private final Map<ServerConnectRequest.Result, Set<String>> results = new HashMap<>();
        private final CommandSender sender;
        private int count = 0;

        public SendCallback(CommandSender sender)
        {
            this.sender = sender;
            for ( ServerConnectRequest.Result result : ServerConnectRequest.Result.values() )
            {
                results.put( result, new LinkedHashSet<>() );
            }
        }

        public void lastEntryDone()
        {
            ComponentBuilder text = new ComponentBuilder();
            text.append( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "command_send_results" ) ) );

            boolean delimiterFlag = false;
            BaseComponent[] delimiter = TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "command_send_delimiter" ) );
            for ( Map.Entry<ServerConnectRequest.Result, Set<String>> entry : results.entrySet() )
            {
                if ( entry.getValue().isEmpty() )
                {
                    continue;
                }

                if ( !delimiterFlag )
                {
                    delimiterFlag = true;
                } else
                {
                    text.append( delimiter, ComponentBuilder.FormatRetention.NONE );
                }

                // Add hover list of player names sent limited by defined constant.
                Collection<String> serverNames = entry.getValue();
                ComponentBuilder hoverBuilder = new ComponentBuilder();
                BaseComponent[] hoverDelimiter = TextComponent.fromLegacyText(
                        ProxyServer.getInstance().getTranslation( "command_send_hover_delimiter" ) );
                BaseComponent[] hoverNamePrefix = TextComponent.fromLegacyText(
                        ProxyServer.getInstance().getTranslation( "command_send_hover_name_prefix" ) );
                int rem = ( serverNames.size() > HOVER_NAMES_LIMIT ) ? serverNames.size() - HOVER_NAMES_LIMIT : 0;
                int i = 0;
                for ( String serverName : serverNames )
                {
                    if ( i != 0 )
                    {
                        hoverBuilder.append( hoverDelimiter, ComponentBuilder.FormatRetention.NONE );
                    }
                    hoverBuilder.append( hoverNamePrefix, ComponentBuilder.FormatRetention.NONE )
                            .append( serverName, ComponentBuilder.FormatRetention.NONE );
                    if ( ++i == HOVER_NAMES_LIMIT )
                    {
                        break;
                    }
                }
                if ( rem > 0 )
                {
                    hoverBuilder.append( hoverDelimiter, ComponentBuilder.FormatRetention.NONE )
                            .append( hoverNamePrefix, ComponentBuilder.FormatRetention.NONE )
                            .append( " and " + rem + " more", ComponentBuilder.FormatRetention.NONE );
                }
                //

                ComponentBuilder serverText = new ComponentBuilder();
                serverText.event( new HoverEvent( HoverEvent.Action.SHOW_TEXT, hoverBuilder.create() ) );
                serverText.append( ProxyServer.getInstance().getTranslation( "command_send_result_title",
                        CaseFormat.UPPER_UNDERSCORE.converterTo( CaseFormat.UPPER_CAMEL ).convert( entry.getKey().name() ) ),
                        ComponentBuilder.FormatRetention.EVENTS );
                serverText.append( TextComponent.fromLegacyText(
                                ProxyServer.getInstance().getTranslation( "command_send_counter", entry.getValue().size() ) ),
                        ComponentBuilder.FormatRetention.EVENTS );

                text.append( serverText.create(), ComponentBuilder.FormatRetention.NONE );
            }
            sender.sendMessage( text.create() );
        }

        public static class Entry implements Callback<ServerConnectRequest.Result>
        {

            private final SendCallback callback;
            private final ProxiedPlayer player;
            private final ServerInfo target;

            public Entry(SendCallback callback, ProxiedPlayer player, ServerInfo target)
            {
                this.callback = callback;
                this.player = player;
                this.target = target;
                this.callback.count++;
            }

            @Override
            public void done(ServerConnectRequest.Result result, Throwable error)
            {
                callback.results.get( result ).add( player.getName() );
                if ( result == ServerConnectRequest.Result.SUCCESS )
                {
                    player.sendMessage( ProxyServer.getInstance().getTranslation( "you_got_summoned", target.getName(), callback.sender.getName() ) );
                }

                if ( --callback.count == 0 )
                {
                    callback.lastEntryDone();
                }
            }
        }
    }

    public CommandSend()
    {
        super( "send", "bungeecord.command.send" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( args.length != 2 )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "send_cmd_usage" ) );
            return;
        }
        ServerInfo server = ProxyServer.getInstance().getServerInfo( args[1] );
        if ( server == null )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "no_server" ) );
            return;
        }

        Set<ProxiedPlayer> targets = null;
        if ( args[0].equalsIgnoreCase( "all" ) )
        {
            targets = new LinkedHashSet<>( ProxyServer.getInstance().getPlayers() );
        } else if ( args[0].equalsIgnoreCase( "current" ) )
        {
            if ( !( sender instanceof ProxiedPlayer ) )
            {
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "player_only" ) );
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;
            targets = new LinkedHashSet<>( player.getServer().getInfo().getPlayers() );
        } else
        {
            // If we use a server name, send the entire server. This takes priority over players.
            ServerInfo serverTarget = ProxyServer.getInstance().getServerInfo( args[0] );
            if ( serverTarget != null )
            {
                targets = new LinkedHashSet<>( serverTarget.getPlayers() );
            } else
            {
                // Support for comma separated list sending
                if ( args[ 0 ].contains( "," ) )
                {
                    String[] names = args[ 0 ].split( "," );
                    for ( String name : names )
                    {
                        ProxiedPlayer player = ProxyServer.getInstance().getPlayer( name );
                        if ( player != null )
                        {
                            if ( targets == null )
                            {
                                targets = new LinkedHashSet<>();
                            }
                            targets.add( player );
                        }
                    }
                } else
                {
                    // Single player selection
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer( args[0] );
                    if ( player != null )
                    {
                        targets = Collections.singleton( player );
                    }
                }

                if ( targets == null )
                {
                    sender.sendMessage( ProxyServer.getInstance().getTranslation( "user_not_online" ) );
                    return;
                }
            }
        }

        if ( targets.isEmpty() )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "command_send_no_players" ) );
            return;
        }

        sender.sendMessage( ProxyServer.getInstance().getTranslation( "command_send_attempting",
                ( targets.size() == 1 ) ? targets.iterator().next().getName() : targets.size() + " players",
                server.getName() ) );

        final SendCallback callback = new SendCallback( sender );
        Map<ProxiedPlayer, ServerConnectRequest> connections = new HashMap<>();
        for ( ProxiedPlayer player : targets )
        {
            ServerConnectRequest request = ServerConnectRequest.builder()
                .target( server )
                .reason( ServerConnectEvent.Reason.COMMAND )
                .callback( new SendCallback.Entry( callback, player, server ) )
                .build();
            connections.put( player, request );
        }
        for ( Map.Entry<ProxiedPlayer, ServerConnectRequest> entry : connections.entrySet() )
        {
            entry.getKey().connect( entry.getValue() );
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        if ( args.length > 2 || args.length == 0 )
        {
            return ImmutableSet.of();
        }

        Set<String> matches = new HashSet<>();
        if ( args.length == 1 )
        {
            boolean flag = !Strings.isNullOrEmpty( args[0] ) && args[0].charAt( args[0].length() - 1 ) == ',';
            String search = args[0].toLowerCase( Locale.ROOT );
            Set<String> existingNames = null;
            for ( ProxiedPlayer player : ProxyServer.getInstance().getPlayers() )
            {
                if ( flag )
                {
                    if ( existingNames == null )
                    {
                        existingNames = new HashSet<>();
                        Collections.addAll( existingNames, search.split( "," ) );
                    }
                    if ( !existingNames.contains( player.getName().toLowerCase( Locale.ROOT ) ) )
                    {
                        matches.add( args[0] + player.getName() );
                    }
                } else if ( player.getName().toLowerCase( Locale.ROOT ).startsWith( search ) )
                {
                    matches.add( player.getName() );
                }
            }
            if ( "all".startsWith( search ) )
            {
                matches.add( "all" );
            }
            if ( "current".startsWith( search ) )
            {
                matches.add( "current" );
            }
        } else
        {
            String search = args[1].toLowerCase( Locale.ROOT );
            for ( String server : ProxyServer.getInstance().getServers().keySet() )
            {
                if ( server.toLowerCase( Locale.ROOT ).startsWith( search ) )
                {
                    matches.add( server );
                }
            }
        }
        return matches;
    }
}
