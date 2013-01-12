package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Permission;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;

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
            if (!args[0].startsWith("&h"))
            {
                builder.append(ChatColor.DARK_PURPLE);
                builder.append("[Alert] ");
            }

            for (String s : args)
            {
                s = s.replace("&h", "");
                builder.append(ChatColor.translateAlternateColorCodes('&', s));
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
