package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command to set a temp copy of the motd in real-time without stopping the
 * proxy.
 */
public class CommandMotd extends Command
{

    public CommandMotd()
    {
        super("bungeecord.command.motd");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        StringBuilder newMOTD = new StringBuilder();
        for (String s : args)
        {
            newMOTD.append(s);
            newMOTD.append(" ");
        }
        BungeeCord.getInstance().config.motd = ChatColor.translateAlternateColorCodes('&', newMOTD.substring(0, newMOTD.length() - 1));
    }
}
