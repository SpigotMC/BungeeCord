
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.CommandExecutionException;
import net.md_5.bungee.command.ConsoleCommandSender;

import java.util.logging.Level;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author michael
 */
public class Test
{

    public static void main(String[] args) throws Exception
    {
        BungeeCord bungee = new BungeeCord();
        ProxyServer.setInstance( bungee );
        bungee.getLogger().info( "Enabled BungeeCord version " + bungee.getVersion() );
        bungee.start();

        while ( bungee.isRunning )
        {
            String line = bungee.getConsoleReader().readLine( ">" );
            try
            {
                if ( !bungee.getPluginManager().dispatchCommand( ConsoleCommandSender.getInstance(), line, null, true ) )
                {
                    bungee.getConsole().sendMessage(ChatColor.RED + "Command not found");
                }
            } catch ( CommandExecutionException ex )
            {
                bungee.getLogger().log( Level.WARNING, "Error in dispatching command", ex );
            }
        }
    }
}
