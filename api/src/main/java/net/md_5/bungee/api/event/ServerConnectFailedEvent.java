package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when an connect to a server has failed, only called when the
 * ProxiedPlayer remains online.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerConnectFailedEvent extends Event
{
    /**
     * Player whom the server is for.
     */
    private final ProxiedPlayer player;
    /**
     * TServer info of the failed destination.
     */
    private final ServerInfo serverInfo;
    /**
     * Throwable reason which caused the connection failure.
     */
    private final Throwable throwable;
    /**
     * The user should receive the failure message.
     */
    private boolean informUser = true;

    public ServerConnectFailedEvent(ProxiedPlayer player, ServerInfo serverInfo, Throwable throwable) {
        this.player = player;
        this.serverInfo = serverInfo;
        this.throwable = throwable;
    }
}
