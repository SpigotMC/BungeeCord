package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerDisconnectEvent extends Event implements Cancellable
{

    /**
     * Player disconnecting from a server.
     */
    @NonNull
    private final ProxiedPlayer player;
    /**
     * Server the player is disconnecting from.
     */
    @NonNull
    private final ServerInfo target;

    /**
     * The server the player will be re-connected to.
     */
    @NonNull
    private ServerInfo fallbackServer = null;

    /**
     * If the player should be connected to the target server.
     */
    private boolean cancelled;
}
