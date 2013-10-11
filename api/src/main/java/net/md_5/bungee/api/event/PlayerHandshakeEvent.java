package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.protocol.packet.Packet2Handshake;

/**
 * Event called to represent a player first making their presence and username
 * known.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PlayerHandshakeEvent extends AsyncEvent<PlayerHandshakeEvent> implements Cancellable
{

    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;
    /**
     * The handshake.
     */
    private final Packet2Handshake handshake;

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Message to use when kicking if this event is canceled.
     */
    private String cancelReason;
    
    public PlayerHandshakeEvent(PendingConnection connection, Packet2Handshake handshake, Callback<PlayerHandshakeEvent> done)
    {
        super( done );
        this.connection = connection;
        this.handshake = handshake;
    }
}
