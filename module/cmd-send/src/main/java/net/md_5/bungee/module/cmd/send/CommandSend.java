package net.md_5.bungee.module.cmd.send;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
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

    protected static class SendCallback
    {

        private final Map<ServerConnectRequest.Result, List<String>> results = new HashMap<>();
        private final CommandSender sender;
        private int count = 0;

        public SendCallback(CommandSender sender)
        {
            this.sender = sender;
            for ( ServerConnectRequest.Result result : ServerConnectRequest.Result.values() )
            {
                results.put( result, new ArrayList<String>() );
            }
        }

        public void lastEntryDone()
        {
            ComponentBuilder text = new ComponentBuilder();
            text.append( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "command_send_results" ) ) );

            boolean delimiterFlag = false;
            BaseComponent[] delimiter = TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "command_send_delimiter" ) );
            for ( Map.Entry<ServerConnectRequest.Result, List<String>> entry : results.entrySet() )
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
                TextComponent serverText = new TextComponent(
                        CaseFormat.UPPER_UNDERSCORE.converterTo( CaseFormat.UPPER_CAMEL ).convert( entry.getKey().name() ) );
                serverText.setColor( ChatColor.getByChar( ProxyServer.getInstance().getTranslation( "command_send_server_title_color_char" ).charAt( 0 ) ) );
                if ( serverText.getExtra() == null )
                {
                    serverText.setExtra( new ArrayList<>() );
                }
                serverText.getExtra().addAll( Arrays.asList(
                        TextComponent.fromLegacyText(
                                ProxyServer.getInstance().getTranslation( "command_send_counter", entry.getValue().size() ) ) ) );

                // Add hoverable list of players limited up to 100.
                List<String> serverNames;
                if ( entry.getValue().size() > 100 )
                {
                    serverNames = entry.getValue().subList( 0, 100 );
                } else
                {
                    serverNames = entry.getValue();
                }

                serverText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder( Joiner.on( ", " ).join( serverNames ) ).color( ChatColor.YELLOW ).create() ) );
                text.append( serverText, ComponentBuilder.FormatRetention.NONE );
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

        List<ProxiedPlayer> targets;
        if ( args[0].equalsIgnoreCase( "all" ) )
        {
            targets = new ArrayList<>( ProxyServer.getInstance().getPlayers() );
        } else if ( args[0].equalsIgnoreCase( "current" ) )
        {
            if ( !( sender instanceof ProxiedPlayer ) )
            {
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "player_only" ) );
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;
            targets = new ArrayList<>( player.getServer().getInfo().getPlayers() );
        } else
        {
            // If we use a server name, send the entire server. This takes priority over players.
            ServerInfo serverTarget = ProxyServer.getInstance().getServerInfo( args[0] );
            if ( serverTarget != null )
            {
                targets = new ArrayList<>( serverTarget.getPlayers() );
            } else
            {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer( args[0] );
                if ( player == null )
                {
                    sender.sendMessage( ProxyServer.getInstance().getTranslation( "user_not_online" ) );
                    return;
                }
                targets = Collections.singletonList( player );
            }
        }

        if ( targets.isEmpty() )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "command_send_no_players" ) );
            return;
        }

        final SendCallback callback = new SendCallback( sender );
        for ( ProxiedPlayer player : targets )
        {
            ServerConnectRequest request = ServerConnectRequest.builder()
                    .target( server )
                    .reason( ServerConnectEvent.Reason.COMMAND )
                    .callback( new SendCallback.Entry( callback, player, server ) )
                    .build();
            player.connect( request );
        }

        sender.sendMessage( ProxyServer.getInstance().getTranslation( "command_send_attempting",
                ( targets.size() == 1 ) ? targets.get( 0 ).getName() : targets.size() + " players",
                server.getName() ) );
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
            String search = args[0].toLowerCase( Locale.ROOT );
            for ( ProxiedPlayer player : ProxyServer.getInstance().getPlayers() )
            {
                if ( player.getName().toLowerCase( Locale.ROOT ).startsWith( search ) )
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
