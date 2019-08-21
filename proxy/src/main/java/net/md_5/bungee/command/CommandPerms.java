package net.md_5.bungee.command;

import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.Util;
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
        Set<String> permissions = new HashSet<>();
        for ( String group : sender.getGroups() )
        {
            permissions.addAll( ProxyServer.getInstance().getConfigurationAdapter().getPermissions( group ) );
        }
        sender.sendMessage( ProxyServer.getInstance().getTranslation( "command_perms_groups", Util.csv( sender.getGroups() ) ) );

        for ( String permission : permissions )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "command_perms_permission", permission ) );
        }
    }
}
