package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.Permission;
import net.md_5.bungee.UserConnection;

public class CommandIP extends Command
{

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (getPermission(sender) != Permission.MODERATOR && getPermission(sender) != Permission.ADMIN)
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
            return;
        }
        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "Please follow this command by a user name");
            return;
        }
        UserConnection user = BungeeCord.instance.connections.get(args[0]);
        if (user == null)
        {
            sender.sendMessage(ChatColor.RED + "That user is not online");
        } else
        {
            sender.sendMessage(ChatColor.BLUE + "IP of " + args[0] + " is " + user.getAddress());
        }
    }
}
