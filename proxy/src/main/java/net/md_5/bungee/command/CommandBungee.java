package net.md_5.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandBungee extends Command
{

    public CommandBungee()
    {
        super( "bungee" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage( "§aBungeeCord by md_5 §7» §bBotFilter от vk.com/Leymooo_s §7» §fhttp://www.rubukkit.org/threads/137038/" );
    }
}
