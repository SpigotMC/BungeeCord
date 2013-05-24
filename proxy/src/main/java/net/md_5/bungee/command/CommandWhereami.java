package net.md_5.bungee.command;

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
    if (args.length != 0) 
    {
        sender.sendMessage( ChatColor.RED + "You do not need to specify a name. If you wish to know where another player is, use /whereis <player>");
    } else {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer( sender.getName() );
        if (sender instanceof ProxiedPlayer)
        {
            sender.sendMessage( ChatColor.BLUE + "You are in the " + player.getServer().getInfo().getName() + "." );
        } else {
            sender.sendMessage( "You are in the console." );
        }
        
        }
    }
}
