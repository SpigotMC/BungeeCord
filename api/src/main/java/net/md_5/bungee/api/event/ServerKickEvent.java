package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Represents a player getting kicked from a server.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerKickEvent extends Event implements Cancellable
{

    /**
     * Cancelled status.
     */
    private boolean cancelled;
    /**
     * Player being kicked.
     */
    private final ProxiedPlayer player;
    /**
     * Kick reason.
     */
    private String kickReason;
    /**
     * Server to send player to if this event is cancelled.
     */
    private ServerInfo cancelServer;

    public ServerKickEvent(ProxiedPlayer player, String kickReason, ServerInfo cancelServer)
    {
        this.player = player;
        this.kickReason = kickReason;
        this.cancelServer = cancelServer;
    }
}
