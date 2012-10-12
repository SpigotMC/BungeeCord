package net.md_5.bungee.command;

import java.util.Collection;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.UserConnection;

/**
 * Command to list all players connected to the proxy.
 */
public class CommandList extends Command {

    @Override
    public void execute(CommandSender sender, String[] args) {
        StringBuilder users = new StringBuilder();
        Collection<UserConnection> connections = BungeeCord.instance.connections.values();

        for (UserConnection con : connections) {
            users.append(con.username);
            users.append(", ");
        }

        users.setLength(users.length() - 2);
        sender.sendMessage(ChatColor.BLUE + "Currently online across all servers (" + connections.size() + "): " + ChatColor.RESET + users);
    }
}
