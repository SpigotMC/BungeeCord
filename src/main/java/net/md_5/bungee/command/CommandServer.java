package net.md_5.bungee.command;

import java.util.Collection;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.UserConnection;

/**
 * Command to list and switch a player between available servers.
 */
public class CommandServer extends Command {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof UserConnection)) {
            return;
        }
        UserConnection con = (UserConnection) sender;
        Collection<String> servers = BungeeCord.instance.config.servers.keySet();
        if (args.length <= 0) {
            StringBuilder serverList = new StringBuilder();
            for (String server : servers) {
                serverList.append(server);
                serverList.append(", ");
            }
            serverList.setLength(serverList.length() - 2);
            con.sendMessage(ChatColor.GOLD + "You may connect to the following servers at this time: " + serverList.toString());
        } else {
            String server = args[0];
            if (!servers.contains(server)) {
                con.sendMessage(ChatColor.RED + "The specified server does not exist");
            } else {
                con.connect(server);
            }
        }
    }
}
