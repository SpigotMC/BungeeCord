package net.md_5.bungee.api;

import java.util.Collection;
import java.util.Map;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Core configuration adaptor for the proxy api.
 *
 * @deprecated This class is subject to rapid change between releases
 */
@Deprecated
public interface ProxyConfig
{

    /**
     * Time before users are disconnected due to no network activity.
     *
     * @return timeout
     */
    int getTimeout();

    /**
     * UUID used for metrics.
     *
     * @return uuid
     */
    String getUuid();

    /**
     * Set of all listeners.
     *
     * @return listeners
     */
    Collection<ListenerInfo> getListeners();

    /**
     * Set of all servers.
     *
     * @return servers
     */
    Map<String, ServerInfo> getServers();

    /**
     * Does the server authenticate with Mojang.
     *
     * @return online mode
     */
    boolean isOnlineMode();

    /**
     * Whether proxy commands are logged to the proxy log.
     *
     * @return log commands
     */
    boolean isLogCommands();

    /**
     * Time in milliseconds to cache server list info from a ping request from
     * the proxy to a server.
     *
     * @return cache time
     */
    int getRemotePingCache();

    /**
     * Returns the player max.
     *
     * @return player limit
     */
    int getPlayerLimit();

    /**
     * A collection of disabled commands.
     *
     * @return disabled commands
     */
    Collection<String> getDisabledCommands();

    /**
     * Time in milliseconds before timing out a clients request to connect to a
     * server.
     *
     * @return connect timeout
     */
    int getServerConnectTimeout();

    /**
     * Time in milliseconds before timing out a ping request from the proxy to a
     * server when attempting to request server list info.
     *
     * @return ping timeout
     */
    int getRemotePingTimeout();

    /**
     * The connection throttle delay.
     *
     * @return throttle
     */
    @Deprecated
    int getThrottle();

    /**
     * Whether the proxy will parse IPs with spigot or not.
     *
     * @return ip forward
     */
    @Deprecated
    boolean isIpForward();

    /**
     * The encoded favicon.
     *
     * @return favicon
     * @deprecated Use #getFaviconObject instead.
     */
    @Deprecated
    String getFavicon();

    /**
     * The favicon used for the server ping list.
     *
     * @return favicon
     */
    Favicon getFaviconObject();
}
