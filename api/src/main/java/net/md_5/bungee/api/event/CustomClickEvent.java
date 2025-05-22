package net.md_5.bungee.api.event;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Called after a {@link ProxiedPlayer} runs a custom action from a chat event
 * or form submission.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
@ApiStatus.Experimental
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
     * The raw data as submitted.
     */
    private final String rawData;
    /**
     * The parsed form data.
     * <br>
     * If a form submission, usually contains a
     * {@code CustomClickEvent.ACTION_KEY} key with the ID of the relevant
     * submission action.
     * <br>
     * If not a form submission, then may be null.
     */
    private final Map<String, String> parsedData;
    /**
     * Cancelled state.
     */
    private boolean cancelled;
}
