package net.md_5.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.AbstractProxyServer;
import net.md_5.bungee.api.plugin.AbstractCommand;

public class CommandBungee extends AbstractCommand
{

    public CommandBungee()
    {
        super( "bungee" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage( ChatColor.BLUE + "This server is running BungeeCord version " + AbstractProxyServer.getInstance().getVersion() + " by md_5" );
    }
}
