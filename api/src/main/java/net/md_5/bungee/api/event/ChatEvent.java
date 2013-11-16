package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
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
}
