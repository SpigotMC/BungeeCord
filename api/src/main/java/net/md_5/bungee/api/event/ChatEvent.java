package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;

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
        if ( !isCommand() )
        {
            return false;
        }
        
        int index = message.indexOf( " " );
        String commandName;
        if ( index == -1 )
        {
            commandName = message.substring( 1 );
        }
        else
        {
            commandName = message.substring( 1, index );
        }
        boolean checkDisabled = getSender() instanceof ProxiedPlayer;
        return ProxyServer.getInstance().getPluginManager().isRunnableCommand( commandName, checkDisabled );
    }
}
