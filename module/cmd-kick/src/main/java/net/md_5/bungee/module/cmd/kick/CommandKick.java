package net.md_5.bungee.module.cmd.kick;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandKick extends Command
{

    public CommandKick()
    {
        super("gkick", "bungeecord.command.kick");
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
                sender.sendMessage( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "user_not_online" ) ) );
                return;
            }

            if ( args.length == 1 )
            {
                player.disconnect( TextComponent.fromLegacyText( ProxyServer.getInstance().getTranslation( "kick_message" ) ) );
            } else
            {
                String[] reason = new String[ args.length - 1 ];
                System.arraycopy( args, 1, reason, 0, reason.length );
                player.disconnect( TextComponent.fromLegacyText( ChatColor.translateAlternateColorCodes( '&', Joiner.on( ' ' ).join( reason ) ) ) );
            }
        }
    }
}
