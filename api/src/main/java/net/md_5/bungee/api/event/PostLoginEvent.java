package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Event called as soon as a connection has a {@link ProxiedPlayer} and is ready
 * to be connected to a server.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PostLoginEvent extends AsyncEvent<PostLoginEvent>
{

    /**
     * The player involved with this event.
     */
    private final ProxiedPlayer player;

    /**
     * The overriden target server to send the player to on first join
     */
    private ServerInfo targetServer;

    public PostLoginEvent(ProxiedPlayer player, Callback<PostLoginEvent> done)
    {
        super( done );
        this.player = player;
    }
}
