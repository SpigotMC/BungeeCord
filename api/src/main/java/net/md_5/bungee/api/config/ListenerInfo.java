package net.md_5.bungee.api.config;

import java.net.InetSocketAddress;
import lombok.Data;

/**
 * Class representing the configuration of a server listener. Used for allowing
 * multiple listeners on different ports.
 */
@Data
public class ListenerInfo
{

    /**
     * Host to bind to.
     */
    private final InetSocketAddress host;
    /**
     * Displayed MOTD.
     */
    private final String motd;
    /**
     * Max amount of slots displayed on the ping page.
     */
    private final int maxPlayers;
    /**
     * Name of the server which users will be taken to by default.
     */
    private final String defaultServer;
    /**
     * Whether reconnect locations will be used, or else the user is simply
     * transferred to the default server on connect.
     */
    private final boolean forceDefault;
}
