package net.md_5.bungee.command;

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
        /* capthca
        BungeeCord.getInstance().config.load();
        BungeeCord.getInstance().stopListeners();
        BungeeCord.getInstance().startListeners();
        BungeeCord.getInstance().getPluginManager().callEvent( new ProxyReloadEvent( sender ) );
        
        sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.RED.toString() + "BungeeCord has been reloaded."
                + " This is NOT advisable and you will not be supported with any issues that arise! Please restart BungeeCord ASAP." );
         captcha */
        sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.RED.toString() + "Данная команда отключена" );
    }
}
