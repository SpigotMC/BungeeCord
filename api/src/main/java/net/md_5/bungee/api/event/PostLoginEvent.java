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
     * The server to which the player will initially be connected.
     */
    private ServerInfo target;

    public PostLoginEvent(ProxiedPlayer player, ServerInfo target, Callback<PostLoginEvent> done)
    {
        super( done );
        this.player = player;
        this.target = target;
    }
}
