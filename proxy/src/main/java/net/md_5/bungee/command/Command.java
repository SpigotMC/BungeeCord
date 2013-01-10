package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Permission;

/**
 * Class which represents a proxy command. The {@link #execute(net.md_5.bungee.command.CommandSender, java.lang.String[])
 * } method will be called to dispatch the command.
 */
public abstract class Command
{

    /**
     * Execute this command.
     *
     * @param sender the sender executing this command
     * @param args the parameters to this command, does not include the '/' or
     * the original command.
     */
    public abstract void execute(CommandSender sender, String[] args);

    public Permission getPermission(CommandSender sender)
    {
        return BungeeCord.instance.config.getPermission(sender);
    }
}
