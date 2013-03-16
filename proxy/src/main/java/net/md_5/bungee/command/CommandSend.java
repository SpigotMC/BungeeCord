package net.md_5.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandSend extends Command
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
            sender.sendMessage( ChatColor.RED + "Not enough arguments, usage: /send <player|all|current> <target>" );
            return;
        }
        ServerInfo target = ProxyServer.getInstance().getServerInfo( args[1] );
        if ( target == null )
        {
            sender.sendMessage( ChatColor.RED + "Target server does not exist" );
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
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;
            for ( ProxiedPlayer p : player.getServer().getInfo().getPlayers() )
            {
                summon( p, target, sender );
            }
        } else
        {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer( args[0] );
            if ( player == null )
            {
                sender.sendMessage( ChatColor.RED + "That player is not online" );
            }
            summon( player, target, sender );
        }
        sender.sendMessage( ChatColor.GREEN + "Successfully summoned player(s)" );
    }

    private void summon(ProxiedPlayer player, ServerInfo target, CommandSender sender)
    {
        if ( player.getServer().getInfo() != target )
        {
            player.connect( target );
            player.sendMessage( ChatColor.GOLD + "Summoned to " + target.getName() + " by " + sender.getName() );
        }
    }
}
