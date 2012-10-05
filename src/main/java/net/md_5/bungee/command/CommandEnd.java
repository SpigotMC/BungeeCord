package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;

public class CommandEnd extends Command {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Only the console can use this command");
        }
        BungeeCord.instance.stop();
    }
}
