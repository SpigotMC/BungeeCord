package net.md_5.bungee.command;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

/**
 * Command to list and switch a player between available servers.
 */
public class CommandServer extends Command implements TabExecutor
{

    public CommandServer()
    {
        super( "gserver", "bungeecord.command.server" );
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
            player.sendMessage( ProxyServer.getInstance().getTranslation( "current_server" ) + player.getServer().getInfo().getName() );

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
            player.sendMessage( ProxyServer.getInstance().getTranslation( "server_list" ) + serverList.toString() );
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
                player.sendMessage( ProxyServer.getInstance().getTranslation( "no_server" ) );
            } else if ( !server.canAccess( player ) )
            {
                player.sendMessage( ProxyServer.getInstance().getTranslation( "no_server_permission" ) );
            } else
            {
                player.connect( server );
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, String[] args)
    {
        return ( args.length != 0 ) ? Collections.EMPTY_LIST : Iterables.transform( Iterables.filter( ProxyServer.getInstance().getServers().values(), new Predicate<ServerInfo>()
        {
            @Override
            public boolean apply(ServerInfo input)
            {
                return input.canAccess( sender );
            }
        } ), new Function<ServerInfo, String>()
        {
            @Override
            public String apply(ServerInfo input)
            {
                return input.getName();
            }
        } );
    }
}
