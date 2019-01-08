package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Command;

import java.util.Locale;
import java.util.Map;

/**
 * Event called when a player sends a message to a server.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ChatEvent extends TargetedEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Text contained in this chat.
     */
    private String message;

    public ChatEvent(Connection sender, Connection receiver, String message)
    {
        super( sender, receiver );
        this.message = message;
    }

    /**
     * Checks whether this message is valid as a command
     *
     * @return if this message is a command
     */
    public boolean isCommand()
    {
        return message.length() > 0 && message.charAt( 0 ) == '/';
    }

    /**
     * Checks whether this message is run on this proxy server
     *
     * @return if this command runs on the proxy
     */
    public boolean isProxyCommand()
    {
        if (!isCommand())
        {
            return false;
        }

        String[] split = message.split( " ", -1 );
        // Check for chat that only contains " "
        if ( split.length == 0 || split[0].isEmpty() )
        {
            return false;
        }

        ProxyServer proxy = ProxyServer.getInstance();
        String commandName = split[0].toLowerCase( Locale.ROOT );
        if ( proxy.getDisabledCommands().contains( commandName ) )
        {
            return false;
        }

        for ( Map.Entry<String, Command> e : proxy.getPluginManager().getCommands() )
        {
            if ( e.getKey().equalsIgnoreCase( commandName ) )
                return true;
        }

        return false;
    }
}
