package net.md_5.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
            sender.sendMessage( new ComponentBuilder( "Please follow this command by a user name" ).color( ChatColor.RED ).create() );
            return;
        }
        ProxiedPlayer user = ProxyServer.getInstance().getPlayer( args[0] );
        if ( user == null )
        {
            sender.sendMessage( new ComponentBuilder( "That user is not online" ).color( ChatColor.RED ).create() );
        } else
        {
            sender.sendMessage( new ComponentBuilder( "IP of " + args[0] + " is " + user.getAddress() ).color( ChatColor.BLUE ).create() );
        }
    }
}
