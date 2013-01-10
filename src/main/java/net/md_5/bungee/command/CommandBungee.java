package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;

public class CommandBungee extends Command
{

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage(ChatColor.BLUE + "This server is running BungeeCord version " + BungeeCord.version + " by md_5");
        sender.sendMessage(ChatColor.BLUE + "Your current permission level is " + getPermission(sender).name());
    }
}
