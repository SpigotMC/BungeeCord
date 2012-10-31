package net.md_5.bungee.command;

import java.util.Collection;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.Permission;
import net.md_5.bungee.UserConnection;

/**
 * Command to switch all connected players to a server
 */
public class CommandSendAll extends Command
{

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (getPermission(sender) != Permission.ADMIN)
        {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
            return;
        }

        if (args.length <= 0)
        {
            sender.sendMessage(ChatColor.RED + "You must supply a server");
        } else
        {
            String server = args[0];
            Collection<String> servers = BungeeCord.instance.config.servers.keySet();
            if (!servers.contains(server))
            {
                sender.sendMessage(ChatColor.RED + "The specified server does not exist");
            } else
            {
                Collection<UserConnection> connections = BungeeCord.instance.connections.values();
                for (UserConnection con : connections)
                {
                    con.connect(server);
                }
            }
        }
    }
}
