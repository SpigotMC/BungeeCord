package net.md_5.bungee.api.config;

import java.util.List;
import net.md_5.bungee.api.CommandSender;

/**
 * This class allows plugins to set their own configuration adapter to load
 * settings from a different place.
 */
public interface ConfigurationAdapter
{

    /**
     * Gets an integer from the specified path.
     *
     * @param path the path to retrieve the integer from
     * @return the retrieved integer
     */
    public int getInt(String path);

    /**
     * Gets a string from the specified path.
     *
     * @param path the path to retrieve the string from.
     * @return the retrieved string
     */
    public String getString(String path);

    /**
     * Get a string list from the specified path.
     *
     * @param path the path to retrieve the list from.
     * @return the retrieved list.
     */
    public List<String> getStringList(String path);

    /**
     * Get the configuration all servers which may be accessible via the proxy.
     *
     * @return all accessible servers
     */
    public List<ServerInfo> getServers();

    /**
     * Get information about all hosts to bind the proxy to.
     *
     * @return a list of all hosts to bind to
     */
    public List<ListenerInfo> getListeners();

    /**
     * Set the permissions of the specified {@link CommandSender}
     *
     * @param sender the sender to set permissions on.
     */
    public void setPermissions(CommandSender sender);
}
