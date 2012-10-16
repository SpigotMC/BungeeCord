package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.Permission;
import net.md_5.bungee.UserConnection;

public class CommandAlert extends Command {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (getPermission(sender) != Permission.ADMIN) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please follow this command by an announcement to make");
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append(ChatColor.DARK_PURPLE);
            builder.append(" [Alert] ");
            for (String s : args) {
                builder.append(s);
            }
            String message = builder.toString();
            for (UserConnection con : BungeeCord.instance.connections.values()) {
                con.sendMessage(message.toString());
            }
        }
    }
}
