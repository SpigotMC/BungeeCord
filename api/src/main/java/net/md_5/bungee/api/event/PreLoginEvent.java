package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.protocol.packet.Packet2Handshake;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called to represent a player logging in.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PreLoginEvent extends Event implements Cancellable
{
    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;
    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Message to use when kicking if this event is canceled.
     */
    private String cancelReason;
    /**
     * The handshake.
     */
    private final Packet2Handshake handshake;

    public PreLoginEvent(PendingConnection connection, Packet2Handshake handshake)
    {
        this.connection = connection;
        this.handshake = handshake;
    }
}
