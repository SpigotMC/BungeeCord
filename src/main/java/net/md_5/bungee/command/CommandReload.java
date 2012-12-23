package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.Permission;

public class CommandReload extends Command
{

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (getPermission(sender) != Permission.ADMIN)
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return;
        }
        BungeeCord.instance.config.load();
        sender.sendMessage(ChatColor.GREEN + "Reloaded config, please restart if you have any issues");
    }
}
