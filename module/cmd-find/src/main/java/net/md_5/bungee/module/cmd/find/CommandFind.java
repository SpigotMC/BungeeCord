package net.md_5.bungee.module.cmd.find;

import java.util.Collections;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.PlayerCommand;

public class CommandFind extends PlayerCommand
{

    public CommandFind()
    {
        super( "find", "bungeecord.command.find" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( args.length != 1 )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "username_needed" ) );
        } else
        {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer( args[0] );
            if ( player == null || player.getServer() == null )
            {
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "user_not_online" ) );
            } else
            {
                boolean moduleLoaded = ProxyServer.getInstance().getPluginManager().getPlugin( "cmd_server" ) != null;
                ServerInfo server = player.getServer().getInfo();
                ComponentBuilder componentBuilder = new ComponentBuilder().appendLegacy( ProxyServer.getInstance().getTranslation( "user_online_at", player.getName(), server.getName() ) );

                if ( moduleLoaded && server.canAccess( sender ) )
                {
                    componentBuilder.event( new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder().appendLegacy( ProxyServer.getInstance().getTranslation( "click_to_connect" ) ).create() )
                    );
                    componentBuilder.event( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/server " + server.getName() ) );
                }

                sender.sendMessage( componentBuilder.create() );
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        return args.length == 1 ? super.onTabComplete( sender, args ) : Collections.emptyList();
    }
}
