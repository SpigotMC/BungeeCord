package net.md_5.bungee.command;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class CommandReload extends Command
{

    public CommandReload()
    {
        super( "greload", "bungeecord.command.reload" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        BungeeCord bungee = BungeeCord.getInstance();
        BungeeCord.getInstance().config.load();
        bungee.reloadMessages();
        bungee.stopListeners();
        bungee.startListeners();
        for ( Plugin plugin : Lists.reverse( new ArrayList<>( bungee.getPluginManager().getPlugins() ) ) )
        {
            bungee.getPluginManager().disablePlugin( plugin );
        }
        bungee.getPluginManager().enablePlugins();
        bungee.getPluginManager().callEvent( new ProxyReloadEvent( sender ) );

        sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.RED.toString() + "BungeeCord has been reloaded."
                + " This is NOT advisable and you will not be supported with any issues that arise! Please restart BungeeCord ASAP." );
    }
}
