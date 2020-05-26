package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called when login process is interrupted by player in result of
 * clicking Cancel button.
 *
 * This event isn't called when {@link PreLoginEvent} or {@link LoginEvent}
 * are cancelled by {@link Cancellable#setCancelled(boolean)}.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class LoginAbortedEvent extends Event
{

    /**
     * Connection which aborted their login process.
     */
    private final PendingConnection connection;
}
