package net.md_5.bungee.api.event;

import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Cancellable;

/**
 * Event called when an 1.13+ player uses tab completion.
 * This event is fired after {@link TabCompleteEvent}.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BrigadierSuggestionsEvent extends TargetedEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * The message the player has already entered.
     */
    private final String cursor;
    /**
     * The suggestions that will be sent to the client. If this list is empty,
     * the request will be forwarded to the server.
     */
    private Suggestions suggestions;

    public BrigadierSuggestionsEvent(Connection sender, Connection receiver, String cursor, Suggestions suggestions)
    {
        super( sender, receiver );
        this.cursor = cursor;
        this.suggestions = suggestions;
    }
}
