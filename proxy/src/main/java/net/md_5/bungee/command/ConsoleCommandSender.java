package net.md_5.bungee.command;

import net.md_5.bungee.ChatColor;

/**
 * Command sender representing the proxy console.
 */
public class ConsoleCommandSender implements CommandSender
{

    public static final ConsoleCommandSender instance = new ConsoleCommandSender();

    @Override
    public void sendMessage(String message)
    {
        System.out.println(ChatColor.stripColor(message));
    }

    @Override
    public String getName()
    {
        return "CONSOLE";
    }
}
