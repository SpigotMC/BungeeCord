package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Cancellable;

/**
 * Event called when a player sends a message to a server.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ChatEvent extends AsyncEvent<ChatEvent> implements Cancellable
{

    /**
     * Creator of the action.
     */
    private final Connection sender;
    /**
     * Receiver of the action.
     */
    private final Connection receiver;
    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Text contained in this chat.
     */
    private String message;

    public ChatEvent(Callback<ChatEvent> callback, Connection sender, Connection receiver, String message)
    {
        super( callback );
        this.sender = sender;
        this.receiver = receiver;
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
