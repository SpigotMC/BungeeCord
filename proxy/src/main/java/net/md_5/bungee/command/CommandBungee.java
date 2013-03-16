package net.md_5.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.netty.HandlerBoss;

public class CommandBungee extends Command
{

    public CommandBungee()
    {
        super( "bungee" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage( ChatColor.BLUE + "This server is running BungeeCord version " + ProxyServer.getInstance().getVersion() + " by md_5" );
        sender.sendMessage( ChatColor.BLUE + "There are currently " + HandlerBoss.registeredChannels + " channels registered" );
    }
}
