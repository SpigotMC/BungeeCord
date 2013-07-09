package net.md_5.bungee.api;

import net.md_5.bungee.api.plugin.PluginManager;
import com.google.common.base.Preconditions;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import lombok.Getter;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.api.tab.CustomTabList;

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
        Preconditions.checkNotNull( instance, "instance" );
        Preconditions.checkArgument( ProxyServer.instance == null, "Instance already set" );
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
     * Gets a localized string from the .properties file.
     *
     * @return the localized string
     */
    public abstract String getTranslation(String name);

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
     * Gets a connected player via their unique username.
     *
     * @param name of the player
     * @return their player instance
     */
    public abstract ProxiedPlayer getPlayer(String name);

    /**
     * Return all servers registered to this proxy, keyed by name. Unlike the
     * methods in {@link ConfigurationAdapter#getServers()}, this will not
     * return a fresh map each time.
     *
     * @return all registered remote server destinations
     */
    public abstract Map<String, ServerInfo> getServers();

    /**
     * Gets the server info of a server.
     *
     * @param name the name of the configured server
     * @return the server info belonging to the specified server
     */
    public abstract ServerInfo getServerInfo(String name);

    /**
     * Get the {@link PluginManager} associated with loading plugins and
     * dispatching events. It is recommended that implementations use the
     * provided PluginManager class.
     *
     * @return the plugin manager
     */
    public abstract PluginManager getPluginManager();

    /**
     * Returns the currently in use configuration adapter.
     *
     * @return the used configuration adapter
     */
    public abstract ConfigurationAdapter getConfigurationAdapter();

    /**
     * Set the configuration adapter to be used. Must be called from
     * {@link Plugin#onLoad()}.
     *
     * @param adapter the adapter to use
     */
    public abstract void setConfigurationAdapter(ConfigurationAdapter adapter);

    /**
     * Get the currently in use reconnect handler.
     *
     * @return the in use reconnect handler
     */
    public abstract ReconnectHandler getReconnectHandler();

    /**
     * Sets the reconnect handler to be used for subsequent connections.
     *
     * @param handler the new handler
     */
    public abstract void setReconnectHandler(ReconnectHandler handler);

    /**
     * Gracefully mark this instance for shutdown.
     */
    public abstract void stop();

    /**
     * Start this instance so that it may accept connections.
     *
     * @throws Exception any exception thrown during startup causing the
     * instance to fail to boot
     */
    public abstract void start() throws Exception;

    /**
     * Register a channel for use with plugin messages. This is required by some
     * server / client implementations.
     *
     * @param channel the channel to register
     */
    public abstract void registerChannel(String channel);

    /**
     * Unregister a previously registered channel.
     *
     * @param channel the channel to unregister
     */
    public abstract void unregisterChannel(String channel);

    /**
     * Get an immutable set of all registered plugin channels.
     *
     * @return registered plugin channels
     */
    public abstract Collection<String> getChannels();

    /**
     * Get the Minecraft version supported by this proxy.
     *
     * @return the supported Minecraft version
     */
    public abstract String getGameVersion();

    /**
     * Get the Minecraft protocol version supported by this proxy.
     *
     * @return the Minecraft protocol version
     */
    public abstract byte getProtocolVersion();

    /**
     * Factory method to construct an implementation specific server info
     * instance.
     *
     * @param name name of the server
     * @param address connectable Minecraft address + port of the server
     * @param motd the motd when used as a forced server
     * @param restricted whether the server info restricted property will be set
     * @return the constructed instance
     */
    public abstract ServerInfo constructServerInfo(String name, InetSocketAddress address, String motd, boolean restricted);

    /**
     * Returns the console overlord for this proxy. Being the console, this
     * command server cannot have permissions or groups, and will be able to
     * execute anything.
     *
     * @return the console command sender of this proxy
     */
    public abstract CommandSender getConsole();

    /**
     * Return the folder used to load plugins from.
     *
     * @return the folder used to load plugin
     */
    public abstract File getPluginsFolder();

    /**
     * Get the scheduler instance for this proxy.
     *
     * @return the in use scheduler
     */
    public abstract TaskScheduler getScheduler();

    /**
     * Get the current number of connected users. The default implementation is
     * more efficient than {@link #getPlayers()} as it does not take a lock or
     * make a copy.
     *
     * @return the current number of connected players
     */
    public abstract int getOnlineCount();

    /**
     * Send the specified message to the console and all connected players.
     *
     * @param message the message to broadcast
     */
    public abstract void broadcast(String message);

    /**
     * Gets a new instance of this proxies custom tab list.
     *
     * @param player the player to generate this list in the context of
     * @return a new {@link CustomTabList} instance
     */
    public abstract CustomTabList customTabList(ProxiedPlayer player);

    /**
     * Gets the commands which are disabled and will not be run on this proxy.
     *
     * @return the set of disabled commands
     */
    public abstract Collection<String> getDisabledCommands();
}
