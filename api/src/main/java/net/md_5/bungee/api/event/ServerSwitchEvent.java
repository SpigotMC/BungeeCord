package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.AbstractEvent;

/**
 * Called when a player has changed servers.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerSwitchEvent extends AbstractEvent
{

    /**
     * Player whom the server is for.
     */
    private final ProxiedPlayer player;
}
