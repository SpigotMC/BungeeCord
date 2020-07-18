package net.md_5.bungee.api.event;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player uses tab completion.
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TabCompleteEvent extends TargetedEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * The message the player has already entered.
     */
    @NotNull
    private final String cursor;
    /**
     * The suggestions that will be sent to the client. This list is mutable. If
     * this list is empty, the request will be forwarded to the server.
     */
    @NotNull
    private final List<String> suggestions;

    public TabCompleteEvent(Connection sender, Connection receiver, @NotNull String cursor, @NotNull List<String> suggestions)
    {
        super( sender, receiver );
        this.cursor = cursor;
        this.suggestions = suggestions;
    }
}
