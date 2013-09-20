package net.md_5.bungee.command;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public abstract class PlayerCommand extends Command implements TabExecutor
{

    public PlayerCommand(String name)
    {
        super( name );
    }

    public PlayerCommand(String name, String permission, String... aliases)
    {
        super( name, permission, aliases );
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        return Iterables.transform( ProxyServer.getInstance().getPlayers(), new Function<ProxiedPlayer, String>()
        {
            @Override
            public String apply(ProxiedPlayer input)
            {
                return input.getDisplayName();
            }
        } );
    }
}
