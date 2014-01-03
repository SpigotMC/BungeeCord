package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called as soon as a connection has a {@link ProxiedPlayer} and is ready
 * to be connected to a server.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PostLoginEvent extends Event
{

    /**
     * The player involved with this event.
     */
    private final ProxiedPlayer player;
}
