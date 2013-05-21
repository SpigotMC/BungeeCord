package net.md_5.bungee.command;

import java.util.Collection;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandWhereis extends Command {

    public CommandWhereis() {
        super( "whereis", "bungeecord.command.whereis" );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) sender.sendMessage( "Please follow this command by a user name");
        else {
        	Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
        	ProxiedPlayer user = null;
        	for (ProxiedPlayer player : players) {
        		if (player.getName().equalsIgnoreCase(args[0])) user = player;
        	}
        	if (user == null) sender.sendMessage( ChatColor.RED + "That user is not online");
        	else sender.sendMessage( ChatColor.BLUE + args[0] + " is in the " + user.getServer().getInfo().getName() + ".");
        }
    }
}
