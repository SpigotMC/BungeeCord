package net.md_5.bungee.api.config;

import java.net.InetSocketAddress;
import java.util.Map;
import lombok.Data;
import net.md_5.bungee.api.tab.TabListHandler;

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
     * Number of players to be shown on the tab list.
     */
    private final int tabListSize;
    /**
     * Name of the server which users will be taken to by default.
     */
    private final String defaultServer;
    /**
     * Name of the server which users will be taken when current server goes
     * down.
     */
    private final String fallbackServer;
    /**
     * Whether reconnect locations will be used, or else the user is simply
     * transferred to the default server on connect.
     */
    private final boolean forceDefault;
    /**
     * A list of host to server name mappings which will force a user to be
     * transferred depending on the host they connect to.
     */
    private final Map<String, String> forcedHosts;
    /**
     * Class used to build tab lists for this player.
     */
    private final Class<? extends TabListHandler> tabList;
    /**
     * Whether to set the local address when connecting to servers.
     */
    private final boolean setLocalAddress;
    /**
     * Whether to pass the ping through when we can reliably get the target
     * server (force default server).
     */
    private final boolean pingPassthrough;
}
