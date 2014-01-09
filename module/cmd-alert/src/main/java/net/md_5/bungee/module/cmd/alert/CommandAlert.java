package net.md_5.bungee.module.cmd.alert;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class CommandAlert extends Command
{

    public CommandAlert()
    {
        super( "alert", "bungeecord.command.alert" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( args.length == 0 )
        {
            sender.sendMessage( ChatColor.RED + "You must supply a message." );
        } else
        {
            StringBuilder builder = new StringBuilder();
            if ( args[0].startsWith( "&h" ) )
            {
                // Remove &h
                args[0] = args[0].substring( 2, args[0].length() );
            } else
            {
                builder.append( ProxyServer.getInstance().getTranslation( "alert" ) );
            }

            for ( String s : args )
            {
                builder.append( ChatColor.translateAlternateColorCodes( '&', s ) );
                builder.append( " " );
            }

            String message = builder.substring( 0, builder.length() - 1 );

            ProxyServer.getInstance().broadcast( message );
        }
    }
}
