package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Cancellable;

import java.util.List;

/**
 * Event called when a player uses tab completion.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TabCompleteEvent extends TargetedEvent implements Cancellable {

    /**
     * The message the player has already entered.
     */
    private final String cursor;
    /**
     * The suggestions that will be sent to the client. This list is mutable. If
     * this list is empty, the request will be forwarded to the server.
     */
    private final List<String> suggestions;
    /**
     * Cancelled state.
     */
    private boolean cancelled;

    public TabCompleteEvent(Connection sender, Connection receiver, String cursor, List<String> suggestions) {
        super(sender, receiver);
        this.cursor = cursor;
        this.suggestions = suggestions;
    }
}
