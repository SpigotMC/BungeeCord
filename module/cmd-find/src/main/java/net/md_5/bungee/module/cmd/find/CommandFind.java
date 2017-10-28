package net.md_5.bungee.module.cmd.find;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
            sender.sendMessage( new ComponentBuilder( "Please follow this command by a user name" ).color( ChatColor.RED ).create() );
        } else
        {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer( args[0] );
            if ( player == null || player.getServer() == null )
            {
                sender.sendMessage( new ComponentBuilder( "That user is not online" ).color( ChatColor.RED ).create() );
            } else
            {
                sender.sendMessage( new ComponentBuilder( args[0] ).color( ChatColor.GREEN ).append( " is online at " ).append( player.getServer().getInfo().getName() ).create() );
            }
        }
    }
}
