package net.md_5.bungee.command;

import java.util.Collection;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandWhereami extends Command {
    
    public CommandWhereami() 
    {
        super( "whereami" );
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
    if (args.length != 0) sender.sendMessage( ChatColor.RED + "You do not need to specify a name. If you wish to know where another player is, use /whereis <player>");
    else {
        Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
        ProxiedPlayer user = null;
    	for (ProxiedPlayer player : players) {
    		if (sender.getName().equalsIgnoreCase(args[0])) user = player;
    	}
    	if (user == null) sender.sendMessage( ChatColor.RED + "Oh god. You are not here!");
    	else sender.sendMessage( ProxyServer.getInstance().getTranslation( "whereami" ) + user.getServer().getInfo().getName() + ".");
        }
    }
}
