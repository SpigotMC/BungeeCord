package net.md_5.bungee.plugin;

import java.net.InetAddress;
import lombok.Data;

/**
 * Event called once a remote connection has begun the login procedure. This
 * event is ideal for IP banning, however must be used with care in other places
 * such as logging at the username has not yet been verified with Mojang.
 */
@Data
public class HandshakeEvent implements Cancellable {

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
