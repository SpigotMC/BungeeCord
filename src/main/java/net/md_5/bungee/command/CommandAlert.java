package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.Permission;
import net.md_5.bungee.UserConnection;

public class CommandAlert extends Command
{

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (getPermission(sender) != Permission.ADMIN)
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return;
        }
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "You must supply a message.");
        } else
        {
            StringBuilder builder = new StringBuilder();
            if (!args[0].contains("&h")) //They want to hide the alert prefix
            {
                builder.append(ChatColor.DARK_PURPLE);
                builder.append("[Alert] "); //No space at start.
            } else
            {
                args[0].replaceAll("&h", ""); //Remove hide control code from message
            }

            for (String s : args)
            {

                builder.append(ChatColor.translateAlternateColorCodes('&', s)); //Allow custom colours
                builder.append(" ");
            }
            String message = builder.substring(0, builder.length() - 1);
            for (UserConnection con : BungeeCord.instance.connections.values())
            {
                con.sendMessage(message);
            }
        }
    }
}
