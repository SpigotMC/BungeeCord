package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.Permission;

/**
 * Command to set a temp copy of the motd in real-time without stopping the
 * proxy.
 */
public class CommandMotd extends Command
{

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (getPermission(sender) != Permission.ADMIN)
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
        } else
        {
            String newMOTD = "";
            for (String s : args)
            {
                newMOTD = newMOTD + s + " ";
            }
            newMOTD = newMOTD.substring(0, newMOTD.length() - 1);
            BungeeCord.instance.config.motd = ChatColor.translateAlternateColorCodes('&', newMOTD);
        }
    }
}
