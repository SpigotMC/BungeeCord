package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class LegacyPlayerHandshakeEvent extends Event
{

    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;
    /**
     * Kick Message.
     */
    private String kickMessage;

    public LegacyPlayerHandshakeEvent(PendingConnection connection, String kickMessage)
    {
        this.connection = connection;
        this.kickMessage = kickMessage;
    }
}