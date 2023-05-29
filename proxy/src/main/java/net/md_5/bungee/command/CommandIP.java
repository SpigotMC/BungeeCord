package net.md_5.bungee.command;

import java.util.Collections;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CommandIP extends PlayerCommand
{

    public CommandIP()
    {
        super( "ip", "bungeecord.command.ip" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( args.length < 1 )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "username_needed" ) );
            return;
        }
        ProxiedPlayer user = ProxyServer.getInstance().getPlayer( args[0] );
        if ( user == null )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "user_not_online" ) );
        } else
        {
            sender.sendMessage( new ComponentBuilder()
                    .appendLegacy( ProxyServer.getInstance().getTranslation( "command_ip", user.getName(), user.getSocketAddress() ) )
                    .event( new ClickEvent( ClickEvent.Action.COPY_TO_CLIPBOARD, user.getSocketAddress().toString() ) )
                    .create()
            );
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        return ( args.length == 1 ) ? super.onTabComplete( sender, args ) : Collections.emptyList();
    }
}
