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
    public int getTimeout();

    /**
     * UUID used for metrics.
     */
    public String getUuid();

    /**
     * Set of all listeners.
     */
    public Collection<ListenerInfo> getListeners();

    /**
     * Set of all servers.
     */
    public TMap<String, ServerInfo> getServers();

    /**
     * Does the server authenticate with mojang
     */
    public boolean isOnlineMode();

    /**
     * Returns the player max.
     */
    public int getPlayerLimit();

    /**
     * A collection of disabled commands.
     */
    public Collection<String> getDisabledCommands();

    /**
     * The connection throttle delay.
     */
    public int getThrottle();

    /**
     * Whether the proxy will parse IPs with spigot or not
     */
    public boolean isIpForward();

    /**
     * The path for the Favicon (I.e. server_icon.png)
     * Should be no more or no less than a 64 by 64 Pixel PNG picture.
     */
    public String getFavicon();
}
