package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
        try{
            ProxyServer.getInstance().getPluginManager().disablePlugins();
            ProxyServer.getInstance().getPluginManager().loadAndEnablePlugins();
        } catch ( Exception e ){
            e.printStackTrace();
            throw new InternalError( "There was an error reloading plugins!" );
        }
        sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.RED.toString() + "BungeeCord has been reloaded."
                + " This is NOT advisable and you will not be supported with any issues that arise! Please restart BungeeCord ASAP." );
    }
}
