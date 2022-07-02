package de.florianmichael.bungee.module.cmd.plugins;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
            StringBuilder pluginsList = new StringBuilder();

            for ( Plugin plugin : proxy.getPluginManager().getPlugins() )
            {
                pluginsList.append( ChatColor.GREEN ).append( plugin.getDescription().getName() ).append( ChatColor.WHITE ).append( ", " );
            }

            String plugins = pluginsList.substring( 0, pluginsList.lastIndexOf( "," ) - 1 );
            String translation = proxy.getTranslation( "command_plugins", proxy.getPluginManager().getPlugins().size(), plugins );

            sender.sendMessage( translation );
        } else
        {
            Plugin plugin = proxy.getPluginManager().getPlugin( args[0] );

            if ( plugin == null )
            {
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "plugin_null" ) );
            } else
            {
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "plugin_success",
                        plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthor() ) );
                sender.sendMessage( plugin.getDescription().getDescription() );
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
