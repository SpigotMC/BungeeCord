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

    public ServerConnectEvent(ProxiedPlayer player, ServerInfo target)
    {
        this.player = player;
        this.target = target;
    }
}
