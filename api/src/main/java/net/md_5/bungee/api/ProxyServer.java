package net.md_5.bungee.api;

import net.md_5.bungee.api.plugin.PluginManager;
import com.google.common.base.Preconditions;
import java.util.Collection;
import lombok.Getter;

public abstract class ProxyServer {

    @Getter
    private static ProxyServer instance;

    public static void setInstance(ProxyServer instance) {
        Preconditions.checkNotNull(instance, "Instance null");
        Preconditions.checkArgument(instance == null, "Instance already set");
        ProxyServer.instance = instance;
    }

    /**
     * Gets the name of the currently running proxy software.
     *
     * @return the name of this instance
     */
    public abstract String getName();

    /**
     * Gets the version of the currently running proxy software.
     *
     * @return the version of this instance
     */
    public abstract String getVersion();

    /**
     * The current number of players connected to this proxy. This total should
     * include virtual players that may be connected from other servers.
     *
     * @return current player count
     */
    public abstract int playerCount();

    /**
     * Return all currently networked connections to this proxy.
     *
     * @return all networked users
     */
    public abstract Collection<ProxyConnection> getConnections();

    /**
     * Get the {@link PluginManager} associated with loading plugins and
     * dispatching events. It is recommended that implementations use the
     * provided PluginManager class.
     *
     * @return the plugin manager
     */
    public abstract PluginManager getPluginManager();
}
