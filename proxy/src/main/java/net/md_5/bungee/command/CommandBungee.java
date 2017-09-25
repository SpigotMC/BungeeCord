package net.md_5.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class CommandBungee extends Command
{

    public CommandBungee()
    {
        super( "bungee" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage( ChatColor.BLUE + "This server is running " + ProxyServer.getInstance().getName() + " version " + ProxyServer.getInstance().getVersion() + " by md_5" );
        sender.sendMessage( ChatColor.BLUE + "Protocol support for 1.7.x by Zartec, ghac and I9hdkill" );
    }
}
