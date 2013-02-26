package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandReload extends Command
{

    public CommandReload()
    {
        super( "greload", "bungeecord.command.reload" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        BungeeCord.getInstance().config.load();
        BungeeCord.getInstance().stopListeners();
        BungeeCord.getInstance().startListeners();
        sender.sendMessage( ChatColor.GREEN + "Reloaded config, please restart if you have any issues" );
    }
}
