package net.md_5.bungee.api;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.PluginManager;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.logging.Logger;
import lombok.Getter;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class ProxyServer
{

    @Getter
    private static ProxyServer instance;

    /**
     * Sets the proxy instance. This method may only be called once per an
     * application.
     *
     * @param instance the new instance to set
     */
    public static void setInstance(ProxyServer instance)
    {
        Preconditions.checkNotNull(instance, "instance");
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
     * Gets the main logger which can be used as a suitable replacement for
     * {@link System#out} and {@link System#err}.
     *
     * @return the {@link Logger} instance
     */
    public abstract Logger getLogger();

    /**
     * Return all players currently connected.
     *
     * @return all connected players
     */
    public abstract Collection<ProxiedPlayer> getPlayers();

    /**
     * Get the {@link PluginManager} associated with loading plugins and
     * dispatching events. It is recommended that implementations use the
     * provided PluginManager class.
     *
     * @return the plugin manager
     */
    public abstract PluginManager getPluginManager();

    /**
     * Set the configuration adapter to be used. Must be called from
     * {@link Plugin#onLoad()}.
     *
     * @param adapter the adapter to use
     */
    public abstract void setConfigurationAdapter(ConfigurationAdapter adapter);
}
