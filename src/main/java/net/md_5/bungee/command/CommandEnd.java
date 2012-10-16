package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.Permission;

/**
 * Command to terminate the proxy instance. May only be used by the console.
 */
public class CommandEnd extends Command
{

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (getPermission(sender) != Permission.ADMIN)
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
        } else
        {
            BungeeCord.instance.stop();
        }
    }
}
