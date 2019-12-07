package net.md_5.bungee.module.cmd.send;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandSend extends Command implements TabExecutor
{

    protected static class SendCallback
    {

        @Getter
        private final Map<ServerConnectRequest.Result, List<String>> results = new HashMap<>();

        private final CommandSender sender;

        public SendCallback(CommandSender sender)
        {
            this.sender = sender;
            for ( ServerConnectRequest.Result result : ServerConnectRequest.Result.values() )
            {
                results.put( result, new ArrayList<String>() );
            }
        }

        @RequiredArgsConstructor
        public static class Impl implements Callback<ServerConnectRequest.Result>
        {

            private final SendCallback callback;
            private final ProxiedPlayer target;

            @Override
            public void done(ServerConnectRequest.Result result, Throwable error)
            {
                callback.results.get( result ).add( target.getName() );
                if ( result == ServerConnectRequest.Result.SUCCESS )
                {
                    target.sendMessage( ProxyServer.getInstance().getTranslation( "you_got_summoned", target.getName(), callback.sender.getName() ) );
                }
            }
        }

    }

    private ScheduledExecutorService executor;

    public CommandSend()
    {
        super( "send", "bungeecord.command.send" );

        executor = Executors.newScheduledThreadPool( 0, new ThreadFactoryBuilder().setNameFormat( "CommandSend Pool Thread #%1$d" ).build() );
    }

    @Override
    public void execute(final CommandSender sender, String[] args)
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

        final SendCallback callback = new SendCallback( sender );
        final int connectTimeout = 5000;
        for ( ProxiedPlayer target : targets )
        {
            ServerConnectRequest request = ServerConnectRequest.builder()
                    .target( server )
                    .reason( ServerConnectEvent.Reason.COMMAND )
                    .callback( new SendCallback.Impl( callback, target ) )
                    .connectTimeout( connectTimeout )
                    .build();
            target.connect( request );
        }

        // Delay before sending the results to ensure the connect timeout has allowed
        sender.sendMessage( ChatColor.DARK_GREEN + "Attempting to send " + targets.size() + " players to " + server.getName());
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                sender.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "Send Results:" );
                for ( Map.Entry<ServerConnectRequest.Result, List<String>> entry : callback.getResults().entrySet() )
                {
                    ComponentBuilder builder = new ComponentBuilder( "" );
                    if ( !entry.getValue().isEmpty() )
                    {
                        builder.event( new HoverEvent( HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder( Joiner.on(", ").join( entry.getValue() ) ).color( ChatColor.YELLOW ).create() ) );
                    }
                    builder.append( entry.getKey().name() + ": " ).color( ChatColor.GREEN );
                    builder.append( "" + entry.getValue().size() ).bold( true );
                    sender.sendMessage( builder.create() );
                }
            }
        };
        executor.schedule( runnable, connectTimeout + 100, TimeUnit.MILLISECONDS );
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
