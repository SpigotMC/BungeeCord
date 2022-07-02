package de.florianmichael.bungee.module.cmd.plugins;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandPlugins extends Command implements TabExecutor
{

    public CommandPlugins()
    {
        super( "gplugins", "bungeecord.command.plugins" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        ProxyServer proxy = ProxyServer.getInstance();

        if ( args.length == 0 )
        {
            Iterable<String> pluginNames = proxy.getPluginManager().getPlugins().stream().map( input -> input.getDescription().getName() ).collect( Collectors.toList() );

            StringBuilder pluginsList = new StringBuilder();

            for ( String pluginName : pluginNames )
            {
                pluginsList.append( ChatColor.GREEN ).append( pluginName ).append( ChatColor.WHITE ).append( ", " );
            }
            String plugins = pluginsList.substring( 0, pluginsList.lastIndexOf( "," ) - 1 );
            String translation = proxy.getTranslation( "command_plugins", proxy.getPluginManager().getPlugins().size(), plugins );

            sender.sendMessage( TextComponent.fromLegacyText( translation ) );
        } else
        {
            Plugin plugin = proxy.getPluginManager().getPlugin( args[0] );

            if ( plugin == null )
            {
                sender.sendMessage( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "plugin_null" ) ) );
            } else
            {
                sender.sendMessage( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "plugin_success",
                        plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthor() ) ) );
                sender.sendMessage( TextComponent.fromLegacyText( plugin.getDescription().getDescription() ) );
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        if ( args.length == 1 )
        {
            Set<String> matches = new HashSet<>();
            String search = args[0].toLowerCase( Locale.ROOT );
            for ( Plugin plugin : ProxyServer.getInstance().getPluginManager().getPlugins() )
            {
                if ( plugin.getDescription().getName().toLowerCase( Locale.ROOT ).startsWith( search ) )
                {
                    matches.add( plugin.getDescription().getName() );
                }
            }
            return matches;
        } else
        {
            return ImmutableSet.of();
        }
    }
}
