package net.md_5.bungee.api.event;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called after a {@link ProxiedPlayer} runs a custom action from a chat event
 * or form submission.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class CustomClickEvent extends Event implements Cancellable
{

    /**
     * Map key containing the form action, if available.
     */
    public static final String ACTION_KEY = "action";
    //
    /**
     * Player who clicked.
     */
    private final ProxiedPlayer player;
    /**
     * Custom action ID.
     */
    private final String id;
    /**
     * Form data, may be null. If a form submission, usually contains a
     * {@code CustomClickEvent.ACTION_KEY} key with the ID of the relevant
     * submission action.
     */
    private final Map<String, String> data;
    /**
     * Cancelled state.
     */
    private boolean cancelled;
}
