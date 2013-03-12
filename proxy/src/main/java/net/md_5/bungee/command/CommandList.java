package net.md_5.bungee.command;

import java.util.Collection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command to list all players connected to the proxy.
 */
public class CommandList extends Command
{

    public CommandList()
    {
        super( "list", "bungeecord.command.list" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        StringBuilder users = new StringBuilder();
        Collection<ProxiedPlayer> connections = ProxyServer.getInstance().getPlayers();

        if ( connections.isEmpty() )
        {
            sender.sendMessage( ChatColor.AQUA + "[scHub] Currently no players online." );
            return;
        }

        for ( ProxiedPlayer player : connections )
        {
            users.append( player.getDisplayName() );
            users.append( ", " );
            users.append( ChatColor.RESET );
        }

        users.setLength( users.length() - 2 );
        sender.sendMessage( ChatColor.AQUA + "[scHub] Currently online across the scPvP network (" + connections.size() + "): " + ChatColor.RESET + users );
    }
}
