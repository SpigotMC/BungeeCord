package net.md_5.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;

/**
 * {@code CommandExecutionException} is thrown when an {@link Command} fails to execute.
 *
 * @see PluginManager#dispatchCommand(CommandSender, String)
 */
public class CommandExecutionException extends Exception
{
    public CommandExecutionException(String message)
    {
        super( message );
    }

    public CommandExecutionException(String message, Throwable cause)
    {
        super( message, cause );
    }

    public CommandExecutionException(Throwable cause)
    {
        super( cause );
    }
}
