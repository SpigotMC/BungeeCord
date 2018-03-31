package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when a server sends a request for a resource pack to the client
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ResourcePackRequestEvent extends TargetedEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Player receiving the response.
     */
    private final ProxiedPlayer player;
    /**
     * The URL of resource pack.
     */
    private final String url;
    /**
     * The hash of resource pack.
     */
    private final String hash;

    public ResourcePackRequestEvent(Connection sender, Connection receiver, ProxiedPlayer player, String url, String hash)
    {
        super( sender, receiver );
        this.player = player;
        this.url = url;
        this.hash = hash;
    }
}
