package net.md_5.bungee.api;

import gnu.trove.map.TMap;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Collection;

/**
 * Core configuration adaptor for the proxy api.
 */
public interface Config {
    /**
     * Time before users are disconnected due to no network activity.
     */
    public abstract int getTimeout();

    /**
     * UUID used for metrics.
     */
    public abstract String getUuid();

    /**
     * Set of all listeners.
     */
    public abstract Collection<ListenerInfo> getListeners();

    /**
     * Set of all servers.
     */
    public abstract TMap<String, ServerInfo> getServers();

    /**
     * Does the server authenticate with mojang
     */
    public abstract boolean isOnlineMode();

    /**
     * Returns the player max.
     */
    public abstract int getPlayerLimit();

    /**
     * A collection of disabled commands.
     */
    public abstract Collection<String> getDisabledCommands();

    /**
     * The connection throttle delay.
     */
    public abstract int getThrottle();

    /**
     * Whether the proxy will parse IPs with spigot or not
     */
    public abstract boolean isIpForward();

    /**
     * The path for the Favicon (I.e. server_icon.png)
     * Should be no more or no less than a 64 by 64 Pixel PNG picture.
     */
    public abstract String getFavicon();
}
