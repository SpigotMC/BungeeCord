package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandPlayerLimit extends Command
{
    
    public CommandPlayerLimit()
    {
        super( "setplayerlimit", "bungeecord.command.playerlimit" );
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( args.length == 0 )
        {
            return;
        }
        BungeeCord.getInstance().config.setPlayerLimit( Integer.parseInt( args[0] ) );
        sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.RED.toString() + "Лимит увеличен" );
    }
}
