package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when a client connection causes an exception to be thrown.
 * Note: the clients connection is closed after this event is fired.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ClientExceptionEvent extends Event
{
    /**
     * The connection that caused the exception.
     * It's either a {@link net.md_5.bungee.api.connection.ProxiedPlayer}
     * or a {@link net.md_5.bungee.api.connection.PendingConnection}.
     */
    private final Connection connection;
    /**
     * The exception that was thrown.
     */
    private final Throwable exception;
}
