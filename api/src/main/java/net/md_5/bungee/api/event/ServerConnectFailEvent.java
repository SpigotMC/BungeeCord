package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * This event will be fired when a player could not connect to a server.
 * This allows people to dynamically change the fallback server to
 * what they feel is most appropriate for each situation.
 */

@Data
@AllArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerConnectFailEvent extends Event {
    /**
     * The player this event is in context to.
     */
    private ProxiedPlayer player;

    /**
     * The server the player was attempting to connect to but failed.
     */
    private ServerInfo attemptingTo;

    /**
     * The server the will fallback to.
     */
    private ServerInfo fallbackServer;

    /**
     * The Throwable that was caught during this causing the player connect to fail.
     */
    private Throwable cause;

    /**
     * Send the player the internal BungeeCord failed message.
     */
    private boolean sendMessage;
}
