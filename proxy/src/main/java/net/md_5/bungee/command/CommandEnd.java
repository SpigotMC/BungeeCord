package net.md_5.bungee.command;

import com.google.common.base.Joiner;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command to terminate the proxy instance. May only be used by the console by
 * default.
 */
public class CommandEnd extends Command
{

    public CommandEnd()
    {
        super( "end", "bungeecord.command.end" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( args.length == 0 )
        {
            BungeeCord.getInstance().stop();
        } else
        {
            BungeeCord.getInstance().stop( ChatColor.translateAlternateColorCodes( '&', Joiner.on( ' ' ).join( args ) ) );
        }
    }
}
