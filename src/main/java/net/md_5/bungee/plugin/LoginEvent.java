package net.md_5.bungee.plugin;

import java.net.InetAddress;
import lombok.Data;

/**
 * Event called to represent a player logging in.
 */
@Data
public class LoginEvent implements Cancellable
{

    /**
     * Canceled state.
     */
    private boolean cancelled;
    /**
     * Message to use when kicking if this event is canceled.
     */
    private String cancelReason;
    /**
     * Username which the player wishes to use.
     */
    private final String username;
    /**
     * IP address of the remote connection.
     */
    private final InetAddress address;
}
