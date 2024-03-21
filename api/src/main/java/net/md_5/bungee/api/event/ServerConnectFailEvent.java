package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Event;

/**
 * This event is called when a connection to a server fails.
 * This can occur when the server is offline, the player cannot login due to version mismatch, or the server is full.
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
