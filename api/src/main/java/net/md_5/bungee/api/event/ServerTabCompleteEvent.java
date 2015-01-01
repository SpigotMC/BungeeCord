package net.md_5.bungee.api.event;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;

/**
 * Event called when a server replies to a tab completion.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ServerTabCompleteEvent extends TargetedEvent
{

    /**
     * The suggestions that will be sent to the client. This list is mutable.
     */
    private final List<String> suggestions;

    /**
     * The cursor that triggered the tab complete
     */
    private final String cursor;

    public ServerTabCompleteEvent(Connection sender, Connection receiver, String cursor, List<String> suggestions)
    {
        super( sender, receiver );
        this.suggestions = suggestions;
        this.cursor = cursor;
    }
}
