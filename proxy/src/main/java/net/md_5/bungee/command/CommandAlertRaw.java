package net.md_5.bungee.command;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Arrays;

public class CommandAlertRaw extends Command
{

    public CommandAlertRaw()
    {
        super( "alertraw", "bungeecord.command.alert" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( args.length == 0 )
        {
            sender.sendMessage( ChatColor.RED + "You must supply a message." );
        } else
        {
            String message = Joiner.on(' ').join( args );

            try
            {
                ProxyServer.getInstance().broadcast( ComponentSerializer.parse( message ) );
            } catch ( Exception e )
            {
                sender.sendMessage(
                        new ComponentBuilder( "An error occurred while parsing your message. (Hover for details)" ).
                                color( ChatColor.RED ).underlined( true ).
                                event( new HoverEvent( HoverEvent.Action.SHOW_TEXT,
                                        new ComponentBuilder( e.getMessage() ).color( ChatColor.RED ).create() ) ).
                                create() );
            }
        }
    }
}
