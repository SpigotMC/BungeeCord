package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when deciding to connect to a server. At the time when this event is
 * called, no connection has actually been made. Cancelling the event will
 * ensure that the connection does not proceed and can be useful to prevent
 * certain players from accessing certain servers.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerConnectEvent extends Event implements Cancellable
{

    /**
     * Player connecting to a new server.
     */
    private final ProxiedPlayer player;
    /**
     * Server the player will be connected to.
     */
    @NonNull
    private ServerInfo target;
    /**
     * Cancelled state.
     */
    private boolean cancelled;
    private final Reason reason;

    @Deprecated
    public ServerConnectEvent(ProxiedPlayer player, ServerInfo target)
    {
        this( player, target, Reason.UNKNOWN );
    }

    public ServerConnectEvent(ProxiedPlayer player, ServerInfo target, Reason reason)
    {
        this.player = player;
        this.target = target;
        this.reason = reason;
    }

    public enum Reason
    {

        /**
         * Redirection to lobby server due to being unable to connect to
         * original server
         */
        LOBBY_FALLBACK,
        /**
         * Execution of a command
         */
        COMMAND,
        /**
         * Redirecting to another server when client loses connection to server
         * due to an exception.
         */
        SERVER_DOWN_REDIRECT,
        /**
         * Redirecting to another server when kicked from original server.
         */
        KICK_REDIRECT,
        /**
         * Plugin message request.
         */
        PLUGIN_MESSAGE,
        /**
         * Initial proxy connect.
         */
        JOIN_PROXY,
        /**
         * Plugin initiated connect.
         */
        PLUGIN,
        /**
         * Unknown cause.
         */
        UNKNOWN
    }
}
