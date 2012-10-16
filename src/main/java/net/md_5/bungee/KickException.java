package net.md_5.bungee;

/**
 * Exception, which when thrown will disconnect the player from the proxy with
 * the specified message.
 */
public class KickException extends RuntimeException
{

    public KickException(String message)
    {
        super(message);
    }
}
