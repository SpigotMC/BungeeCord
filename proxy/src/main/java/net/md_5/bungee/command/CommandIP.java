package net.md_5.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.AbstractProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CommandIP extends AbstractPlayerCommand
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
            sender.sendMessage( ChatColor.RED + "Please follow this command by a user name" );
            return;
        }
        ProxiedPlayer user = AbstractProxyServer.getInstance().getPlayer( args[0] );
        if ( user == null )
        {
            sender.sendMessage( ChatColor.RED + "That user is not online" );
        } else
        {
            sender.sendMessage( ChatColor.BLUE + "IP of " + args[0] + " is " + user.getAddress() );
        }
    }
}
