package net.md_5.bungee.module.cmd.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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
            player.sendMessage( ProxyServer.getInstance().getTranslation( "current_server", player.getServer().getInfo().getName() ) );
            TextComponent serverList = new TextComponent( ProxyServer.getInstance().getTranslation( "server_list" ) );
            serverList.setColor( ChatColor.GOLD );
            boolean first = true;
            for ( ServerInfo server : servers.values() )
            {
                if ( server.canAccess( player ) )
                {
                    TextComponent serverTextComponent = new TextComponent( first ? server.getName() : ", " + server.getName() );
                    int count = server.getPlayers().size();
                    serverTextComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder( count + ( count == 1 ? " player" : " players" ) + "\n" )
                            .append( "Click to connect to the server" ).italic( true )
                            .create() ) );
                    serverTextComponent.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/server " + server.getName() ) );
                    serverList.addExtra( serverTextComponent );
                    first = false;
                }
            }
            player.sendMessage( serverList );
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
    public Iterable<String> onTabComplete(final CommandSender sender, String[] args) {
        if (args.length <= 1)
        {
            List<String> completions = new ArrayList<String>();
            for (ServerInfo server : ProxyServer.getInstance().getServers().values())
            {
                String serverName = server.getName();
                if (args.length == 1)
                {
                    if (serverName.toLowerCase().startsWith(args[0].toLowerCase()))
                    {
                        completions.add(serverName);
                    }
                } else
                {
                    completions.add(serverName);
                }
            }
            return completions;
        } else
        {
            return Collections.emptyList();
        }
    }
}
