package net.md_5.bungee.command;

import java.util.Map;
import java.util.Map.Entry;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command to list and switch a player between available servers.
 */
public class CommandServer extends Command
{

    public CommandServer()
    {
        super( "server", "bungeecord.command.server" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( !( sender instanceof ProxiedPlayer ) )
        {
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if( BungeeCord.jailServerName.equalsIgnoreCase( player.getServer().getInfo().getName() ) ) {
            player.sendMessage( ChatColor.RED + "You are on the jail server.  If you are not jailed, type /jret to return." );
            return;
        }
        Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
        if ( args.length == 0 )
        {
            StringBuilder serverList = new StringBuilder();
            for ( ServerInfo server : servers.values() )
            {
                if ( server.canAccess( player ) )
                {
                    serverList.append( server.getName() );
                    serverList.append( ", " );
                }
            }
            if ( serverList.length() != 0 )
            {
                serverList.setLength( serverList.length() - 2 );
            }
            player.sendMessage( ChatColor.GOLD + "You may connect to the following servers at this time: " + serverList.toString() );
        } else
        {
            //ServerInfo server = servers.get( args[0] );
            ServerInfo server = null;
            for( Entry<String,ServerInfo> entry : servers.entrySet() ) {
                if( entry.getKey().equalsIgnoreCase( args[0] ) )
                    server = entry.getValue();
            }
            if ( server == null )
            {
                player.sendMessage( ChatColor.RED + "The specified server does not exist" );
            } else if ( !server.canAccess( player ) )
            {
                player.sendMessage( ChatColor.RED + "You don't have permission to access this server" );
            } else
            {
                player.connect( server );
            }
        }
    }
}
