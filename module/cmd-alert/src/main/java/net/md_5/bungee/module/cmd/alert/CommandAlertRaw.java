package net.md_5.bungee.module.cmd.alert;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.chat.ComponentSerializer;

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
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "message_needed" ) );
        } else
        {
            String message = String.join( " ", args );

            try
            {
                ProxyServer.getInstance().broadcast( ComponentSerializer.parse( message ) );
            } catch ( Exception e )
            {
                Throwable error = e;
                while ( error.getCause() != null )
                {
                    error = error.getCause();
                }
                if ( sender instanceof ProxiedPlayer )
                {
                    sender.sendMessage( new ComponentBuilder( ProxyServer.getInstance().getTranslation( "error_occurred_player" ) )
                            .event( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( error.getMessage() )
                                    .color( ChatColor.RED )
                                    .create() ) )
                            .create()
                    );
                } else
                {
                    sender.sendMessage( ProxyServer.getInstance().getTranslation( "error_occurred_console", error.getMessage() ) );
                }
            }
        }
    }
}
