package net.md_5.bungee.module.cmd.plugins;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Command to list all proxy plugins
 */
public class CommandPlugins extends Command
{

    public CommandPlugins()
    {
        super( "gplugins", "bungeecord.command.plugins" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        ProxyServer proxy = ProxyServer.getInstance();
        Iterable<String> pluginNames = Iterables.transform( proxy.getPluginManager().getPlugins(), new Function<Plugin, String>()
        {

            @Override
            public String apply(Plugin input)
            {
                return input.getDescription().getName();
            }
        } );
        StringBuilder pluginsList = new StringBuilder();
        for ( String pluginName : pluginNames )
        {
            pluginsList.append( ChatColor.GREEN ).append( pluginName ).append( ChatColor.WHITE ).append( ", " );
        }
        String plugins = pluginsList.substring( 0, pluginsList.lastIndexOf( "," ) - 1 );
        String translation = proxy.getTranslation( "cmd_plugins", proxy.getPluginManager().getPlugins().size(), plugins );
        sender.sendMessage( TextComponent.fromLegacyText( translation ) );
    }
}
