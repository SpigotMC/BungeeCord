package net.md_5.bungee.module.cmd.kick;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandKick extends Command implements TabExecutor
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

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        if ( args.length == 1 )
        {
            Set<String> matches = new HashSet<>();
            String search = args[0].toLowerCase( Locale.ROOT );
            for ( ProxiedPlayer player : ProxyServer.getInstance().getPlayers() )
            {
                if ( player.getName().toLowerCase( Locale.ROOT ).startsWith( search ) )
                {
                    matches.add( player.getName() );
                }
            }
            return matches;
        } else
        {
            return ImmutableSet.of();
        }
    }
}
