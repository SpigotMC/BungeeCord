package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Event;

/**
 * Not to be confused with {@link ServerConnectEvent}, this event is called once
 * a connection to a server is fully operational, and is about to hand over
 * control of the session to the player. It is useful if you wish to send
 * information to the server before the player logs in.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerConnectedEvent extends Event
{

    /**
     * Player whom the server is for.
     */
    private final ProxiedPlayer player;
    /**
     * The server itself.
     */
    private final Server server;
}
