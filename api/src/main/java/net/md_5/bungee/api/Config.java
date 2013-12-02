package net.md_5.bungee.api;

import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Collection;
import java.util.Map;

/**
 * Core configuration adaptor for the proxy api.
 */
public interface Config {
    /**
     * Time before users are disconnected due to no network activity.
     */
     int getTimeout();

    /**
     * UUID used for metrics.
     */
     String getUuid();

    /**
     * Set of all listeners.
     */
     Collection<ListenerInfo> getListeners();

    /**
     * Set of all servers.
     */
     Map<String, ServerInfo> getServers();

    /**
     * Does the server authenticate with mojang
     */
     boolean isOnlineMode();

    /**
     * Returns the player max.
     */
     int getPlayerLimit();

    /**
     * A collection of disabled commands.
     */
     Collection<String> getDisabledCommands();

    /**
     * The connection throttle delay.
     */
    @Deprecated
     int getThrottle();

    /**
     * Whether the proxy will parse IPs with spigot or not
     */
    @Deprecated
     boolean isIpFoward();

    /**
     * The path for the Favicon (I.e. server_icon.png)
     * Should be no more or no less than a 64 by 64 Pixel PNG picture.
     */
    @Deprecated
     String getFavicon();
}
