package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Event;

/**
 * This event is called when a connection to a server fails (e.g. the server is down).
 * Only called when the server cannot be connected to, and is not called when kicked from a server (e.g. the server is full).
 * Cancelling this event will cancel the transfer to the fallback lobby and prevent the default error message from being shown.
 * <p>
 * If you are using {@link ProxiedPlayer#connect}, this event will be called after the callback is called.
 * So, you can't cancel the callback by cancelling this event.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerConnectFailEvent extends Event
{

    /**
     * Player whom the server is for.
     */
    private final ProxiedPlayer player;
    /**
     * The server itself.
     */
    private final Server server;
    /**
     * Request used to connect to given server.
     */
    private final ServerConnectRequest request;
    /**
     * Cancelled state.
     */
    private boolean cancelled;
}
