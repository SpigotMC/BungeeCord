package net.md_5.bungee.api.plugin;

public class CommandExecutionException extends RuntimeException
{
    public CommandExecutionException()
    {
    }

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
