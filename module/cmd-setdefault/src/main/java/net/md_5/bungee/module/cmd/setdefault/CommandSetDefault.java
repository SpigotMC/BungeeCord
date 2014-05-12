package net.md_5.bungee.module.cmd.setdefault;

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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Command to list and switch a player between available servers.
 */
public class CommandSetDefault extends Command implements TabExecutor
{

    public CommandSetDefault()
    {
        super( "setdefault", "bungeecord.command.setdefault" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
        if ( args.length == 0 )
        {

            TextComponent serverList = new TextComponent( ProxyServer.getInstance().getTranslation( "set_default_server_list" ) );
            serverList.setColor( ChatColor.GOLD );
            boolean first = true;
            for ( ServerInfo server : servers.values() )
            {
                if ( server.canAccess( sender ) )
                {
                    TextComponent serverTextComponent = new TextComponent( first ? server.getName() : ", " + server.getName() );
                    int count = server.getPlayers().size();
                    serverTextComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder( count + (count == 1 ? " sender" : " players") + "\n")
                                    .append( "Click to set as default server" ).italic( true )
                                    .create() ) );
                    serverTextComponent.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/setdefault " + server.getName() ) );
                    serverList.addExtra( serverTextComponent );
                    first = false;
                }
            }
            sender.sendMessage( serverList );
        } else
        {
            ServerInfo server = servers.get( args[0] );
            if ( server == null )
            {
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "no_server" ) );
            } else
            {
                ProxyServer.getInstance().setDefaultServer( args[0] );
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "default_set") );
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
