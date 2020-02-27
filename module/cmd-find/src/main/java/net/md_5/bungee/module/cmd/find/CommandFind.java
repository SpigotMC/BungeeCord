package net.md_5.bungee.module.cmd.find;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.command.PlayerCommand;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

public class CommandFind extends PlayerCommand implements TabExecutor
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
                sender.sendMessage( ProxyServer.getInstance().getTranslation( "user_online_at", player.getName(), player.getServer().getInfo().getName() ) );
            }
        }
    }
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        if ( args.length != 1 )
        {
            return ImmutableSet.of();
        }
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
    }
}
