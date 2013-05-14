package net.md_5.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CommandDefault extends CommandServer {

    @Override
    public void execute( CommandSender sender, String[] args ) {
        if ( !( sender instanceof ProxiedPlayer ) )
        {
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        super.execute( sender, new String[] { player.getPendingConnection().getListener().getDefaultServer() } );
    }

    public String getName() {
        return "default";
    }

    public String[] getAliases() {
        return new String[] { "nexus", "hub" };
    }
}
