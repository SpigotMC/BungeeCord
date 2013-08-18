/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.md_5.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

/**
 *
 * @author Matty
 */
public class CommandReloadPlugins extends Command
{
    public CommandReloadPlugins()
    {
       super( "reloadplugins ", "bungeecord.command.reloadplugins" );
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        ProxyServer.getInstance().getPluginManager().reloadPlugins();
        sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "BungeeCord plugins has been reloaded." );
    }
    
}
