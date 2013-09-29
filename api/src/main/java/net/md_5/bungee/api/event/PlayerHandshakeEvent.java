package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.protocol.packet.Packet2Handshake;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called to represent a player first making their presence and username
 * known.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PlayerHandshakeEvent extends Event
{

    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;
    /**
     * The handshake.
     */
    private final Packet2Handshake handshake;

    public PlayerHandshakeEvent(PendingConnection connection, Packet2Handshake handshake)
    {
        this.connection = connection;
        this.handshake = handshake;
    }
}
