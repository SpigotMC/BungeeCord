package net.md_5.bungee.api.config;

import java.util.Collection;

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
    public Collection<String> getStrings(String path);

    /**
     * Get the configuration all servers which may be accessible via the proxy.
     *
     * @return all accessible servers
     */
    public Collection<ServerInfo> getServers();

    /**
     * Get information about all hosts to bind the proxy to.
     *
     * @return a list of all hosts to bind to
     */
    public Collection<ListenerInfo> getListeners();

    /**
     * Get all groups this player is in.
     *
     * @param player the player to check
     * @return all the player's groups.
     */
    public Collection<String> getGroups(String player);

    /**
     * Get all permission corresponding to the specified group. The result of
     * this method may or may not be cached, depending on the implementation.
     *
     * @param group the group to check
     * @return all true permissions for this group
     */
    public Collection<String> getPermissions(String group);
}
