package net.md_5.bungee.command;

import java.util.Map;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
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
        super( "connect", "bungeecord.command.server" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( !( sender instanceof ProxiedPlayer ) )
        {
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        Map<String, ServerInfo> servers = BungeeCord.getInstance().config.getServers();
        if ( args.length == 0 )
        {
            StringBuilder serverList = new StringBuilder();
            for ( String server : servers.keySet() )
            {
                serverList.append( server );
                serverList.append( ", " );
            }
            serverList.setLength( serverList.length() - 2 );
            player.sendMessage( ChatColor.AQUA + "[scHub] Servers currently online on the scPvP network: " + serverList.toString() );
        } else
        {
            ServerInfo server = servers.get( args[0] );
            if ( server == null )
            {
                player.sendMessage( ChatColor.RED + "[scHub] That server doesn't exist! Make sure you have typed it correctly in lower case." );
            } else if ( server.equals( player.getServer().getInfo() ) )
            {
                player.sendMessage( ChatColor.RED + "[scHub] You are already on that server!" );
            } else
            {
                player.connect( server );
            }
        }
    }
}
