package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called to represent a player, who disconnected during the {@link LoginEvent} and before the {@link PostLoginEvent}
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class LoginCancelledEvent extends Event
{
    /**
     * Connection that failed to login
     */
    private final PendingConnection connection;
}
