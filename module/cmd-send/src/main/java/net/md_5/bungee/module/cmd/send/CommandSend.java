package net.md_5.bungee.module.cmd.send;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandSend extends Command implements TabExecutor
{

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
        ServerInfo target = ProxyServer.getInstance().getServerInfo( args[1] );
        if ( target == null )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "no_server" ) );
            return;
        }

        if ( args[0].equalsIgnoreCase( "all" ) )
        {
            for ( ProxiedPlayer p : ProxyServer.getInstance().getPlayers() )
            {
                summon( p, target, sender );
            }
        } else if ( args[0].equalsIgnoreCase( "current" ) )
        {
            if ( !( sender instanceof ProxiedPlayer ) )
            {
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "player_only" ) );
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;
            for ( ProxiedPlayer p : player.getServer().getInfo().getPlayers() )
            {
                summon( p, target, sender );
            }
        } else
        {
            // If we use a server name, send the entire server. This takes priority over players.
            ServerInfo serverTarget = ProxyServer.getInstance().getServerInfo( args[0] );
            if ( serverTarget != null )
            {
                for ( ProxiedPlayer p : serverTarget.getPlayers() )
                {
                    summon( p, target, sender );
                }
            } else
            {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer( args[0] );
                if ( player == null )
                {
                    sender.sendMessage( ProxyServer.getInstance().getTranslation( "user_not_online" ) );
                    return;
                }
                summon( player, target, sender );
            }
        }
        sender.sendMessage( ProxyServer.getInstance().getTranslation( "successfully_summoned" ) );
    }

    private void summon(ProxiedPlayer player, ServerInfo target, CommandSender sender)
    {
        if ( player.getServer() != null && !player.getServer().getInfo().equals( target ) )
        {
            player.connect( target, ServerConnectEvent.Reason.COMMAND );
            player.sendMessage( ProxyServer.getInstance().getTranslation( "you_got_summoned", target.getName(), sender.getName() ) );
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
