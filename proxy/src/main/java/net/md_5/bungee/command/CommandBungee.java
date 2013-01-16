package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandBungee extends Command
{

    public CommandBungee()
    {
        super("bungee");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage(ChatColor.BLUE + "This server is running BungeeCord version " + BungeeCord.version + " by md_5");
    }
}
