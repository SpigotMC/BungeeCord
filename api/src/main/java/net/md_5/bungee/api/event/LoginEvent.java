package net.md_5.bungee.api.event;

import lombok.Data;
import net.md_5.bungee.api.PendingConnection;
import net.md_5.bungee.api.plugin.Cancellable;

/**
 * Event called to represent a player logging in.
 */
@Data
public class LoginEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Message to use when kicking if this event is canceled.
     */
    private String cancelReason;
    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;
}
