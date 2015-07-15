package net.md_5.bungee.module.cmd.send;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
            sender.sendMessage( ChatColor.RED + "Not enough arguments, usage: /send <server|player|all|current> <target>" );
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
                sender.sendMessage( ChatColor.RED + "Only in game players can use this command" );
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
                    sender.sendMessage( ChatColor.RED + "That player is not online" );
                    return;
                }
                summon( player, target, sender );
            }
        }
        sender.sendMessage( ChatColor.GREEN + "Successfully summoned player(s)" );
    }

    private void summon(ProxiedPlayer player, ServerInfo target, CommandSender sender)
    {
        if ( player.getServer() != null && !player.getServer().getInfo().equals( target ) )
        {
            player.connect( target );
            player.sendMessage( ChatColor.GOLD + "Summoned to " + target.getName() + " by " + sender.getName() );
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
            String search = args[0].toLowerCase();
            for ( ProxiedPlayer player : ProxyServer.getInstance().getPlayers() )
            {
                if ( player.getName().toLowerCase().startsWith( search ) )
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
            String search = args[1].toLowerCase();
            for ( String server : ProxyServer.getInstance().getServers().keySet() )
            {
                if ( server.toLowerCase().startsWith( search ) )
                {
                    matches.add( server );
                }
            }
        }
        return matches;
    }
}
