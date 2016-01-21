package net.md_5.bungee.command;

import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.AbstractProxyServer;
import net.md_5.bungee.api.plugin.AbstractCommand;

public class CommandPerms extends AbstractCommand
{

    public CommandPerms()
    {
        super( "perms" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        Set<String> permissions = new HashSet<>();
        for ( String group : sender.getGroups() )
        {
            permissions.addAll( AbstractProxyServer.getInstance().getConfigurationAdapter().getPermissions( group ) );
        }
        sender.sendMessage( ChatColor.GOLD + "You have the following groups: " + Util.csv( sender.getGroups() ) );

        for ( String permission : permissions )
        {
            sender.sendMessage( ChatColor.BLUE + "- " + permission );
        }
    }
}
