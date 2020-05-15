package net.md_5.bungee.module.cmd.find;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
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
            sender.sendMessage( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "username_needed" ) ) );
        } else
        {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer( args[0] );
            if ( player == null || player.getServer() == null )
            {
                sender.sendMessage( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "user_not_online" ) ) );
            } else
            {
                sender.sendMessage( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "user_online_at", player.getName(), player.getServer().getInfo().getName() ) ) );
            }
        }
    }
}
