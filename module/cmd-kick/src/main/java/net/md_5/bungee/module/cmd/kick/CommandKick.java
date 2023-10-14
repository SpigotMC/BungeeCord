package net.md_5.bungee.module.cmd.kick;

import com.google.common.base.Joiner;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.PlayerCommand;

public class CommandKick extends PlayerCommand
{

    public CommandKick()
    {
        super( "gkick", "bungeecord.command.kick" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( args.length == 0 )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "username_needed" ) );
        } else
        {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer( args[0] );

            if ( player == null )
            {
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "user_not_online" ) );
                return;
            }

            if ( args.length == 1 )
            {
                player.disconnect( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "kick_message" ) ) );
            } else
            {
                player.disconnect( TextComponent.fromLegacyText( ChatColor.translateAlternateColorCodes( '&', Joiner.on( " " ).join( Stream.of( args ).skip( 1 ).iterator() ) ) ) );
            }
        }
    }
}
