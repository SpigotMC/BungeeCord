package net.md_5.bungee.command;

import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class CommandPerms extends Command
{

    public CommandPerms()
    {
        super( "perms" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        StringBuilder groups = new StringBuilder();
        Set<String> permissions = new HashSet<>();
        for ( String group : sender.getGroups() )
        {
            groups.append( group );
            groups.append( ", " );
            permissions.addAll( ProxyServer.getInstance().getConfigurationAdapter().getPermissions( group ) );
        }

        if (groups.length() == 0) {
            sender.sendMessage( ChatColor.GOLD + "You have no groups" );
        } else {
            sender.sendMessage( ChatColor.GOLD + "You have the following groups: " + groups.substring( 0, groups.length() - 2 ) );
        }

        for ( String permission : permissions )
        {
            sender.sendMessage( ChatColor.BLUE + "- " + permission );
        }
    }
}
