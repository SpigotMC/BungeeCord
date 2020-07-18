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
 * Event called when a backend server sends a response to a player asking to
 * tab-complete a chat message or command. Note that this is not called when
 * BungeeCord or a plugin responds to a tab-complete request. Use
 * {@link TabCompleteEvent} for that.
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TabCompleteResponseEvent extends TargetedEvent implements Cancellable
{

    /**
     * Whether the event is cancelled.
     */
    private boolean cancelled;

    /**
     * Mutable list of suggestions sent back to the player. If this list is
     * empty, an empty list is sent back to the client.
     */
    @NotNull
    private final List<String> suggestions;

    public TabCompleteResponseEvent(Connection sender, Connection receiver, @NotNull List<String> suggestions)
    {
        super( sender, receiver );
        this.suggestions = suggestions;
    }
}
