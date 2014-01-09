package net.md_5.bungee.module.cmd.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command to list all players connected to the proxy.
 */
public class CommandList extends Command
{

    public CommandList()
    {
        super( "glist", "bungeecord.command.list" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        for ( ServerInfo server : ProxyServer.getInstance().getServers().values() )
        {
            if ( !server.canAccess( sender ) )
            {
                continue;
            }

            List<String> players = new ArrayList<>();
            for ( ProxiedPlayer player : server.getPlayers() )
            {
                players.add( player.getDisplayName() );
            }
            Collections.sort( players, String.CASE_INSENSITIVE_ORDER );

            sender.sendMessage( ProxyServer.getInstance().getTranslation( "command_list", server.getName(), server.getPlayers().size(), Util.format( players, ChatColor.RESET + ", " ) ) );
        }

        sender.sendMessage( ProxyServer.getInstance().getTranslation( "total_players", ProxyServer.getInstance().getOnlineCount() ) );
    }
}
