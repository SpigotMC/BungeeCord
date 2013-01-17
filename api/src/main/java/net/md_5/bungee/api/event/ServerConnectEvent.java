package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Event;

@Data
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ServerConnectEvent extends Event
{

    /**
     * Player connecting to a new server.
     */
    private final ProxiedPlayer player;
    /**
     * Server the player will be connected to.
     */
    private Server target;
}
