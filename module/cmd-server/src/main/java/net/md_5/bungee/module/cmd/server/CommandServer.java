package net.md_5.bungee.module.cmd.server;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.Map;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.protocol.packet.Chat;

/**
 * Command to list and switch a player between available servers.
 */
public class CommandServer extends Command implements TabExecutor
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
        Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
        if ( args.length == 0 )
        {
            player.sendMessage( ProxyServer.getInstance().getTranslation( "current_server" ) + player.getServer().getInfo().getName() );

            StringBuilder serverList = new StringBuilder("{\"text\":\"" + ProxyServer.getInstance().getTranslation( "server_list" ) + "\",\"extra\":[");
            boolean first = true;
            for ( ServerInfo server : servers.values() )
            {
                if ( server.canAccess( player ) )
                {
                    serverList.append( "{\"text\":\"" + (first ? server.getName() : (", " + server.getName())) + "\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/server " + server.getName() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + server.getPlayers().size() + " players\"}},");
                    first = false;
                }
            }
            if ( serverList.length() != 0 )
            {
                serverList.setLength( serverList.length() - 1 );
            }
            serverList.append("]}");
            player.unsafe().sendPacket(new Chat(serverList.toString()));
        } else
        {
            ServerInfo server = servers.get( args[0] );
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
