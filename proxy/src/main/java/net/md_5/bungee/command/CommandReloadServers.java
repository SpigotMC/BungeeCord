package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;



public class CommandReloadServers extends Command
{

    public CommandReloadServers()
    {
        super( "greloadservers", "bungeecord.command.reloadservers" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        BungeeCord.getInstance().config.loadServers( BungeeCord.getInstance().getConfigurationAdapter(), true );
        sender.sendMessage( "§cServers reloaded" );
    }
}
