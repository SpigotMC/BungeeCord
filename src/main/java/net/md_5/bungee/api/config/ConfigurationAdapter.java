package net.md_5.bungee.api.config;

import java.util.Collection;
import java.util.Map;

/**
 * This class allows plugins to set their own configuration adapter to load
 * settings from a different place.
 */
public interface ConfigurationAdapter {

    /**
     * Reload all the possible values, and if necessary cache them for
     * individual getting.
     */
    void load();

    /**
     * Gets an integer from the specified path.
     *
     * @param path the path to retrieve the integer from
     * @param def  the default value
     * @return the retrieved integer
     */
    int getInt(String path, int def);

    /**
     * Gets a string from the specified path.
     *
     * @param path the path to retrieve the string from.
     * @param def  the default value
     * @return the retrieved string
     */
    String getString(String path, String def);

    /**
     * Gets a boolean from the specified path.
     *
     * @param path the path to retrieve the boolean form.
     * @param def  the default value
     * @return the retrieved boolean
     */
    boolean getBoolean(String path, boolean def);

    /**
     * Get a list from the specified path.
     *
     * @param path the path to retrieve the list form.
     * @param def  the default value
     * @return the retrieved list
     */
    Collection<?> getList(String path, Collection<?> def);

    /**
     * Get the configuration all servers which may be accessible via the proxy.
     *
     * @return all accessible servers, keyed by name
     */
    Map<String, ServerInfo> getServers();

    /**
     * Get information about all hosts to bind the proxy to.
     *
     * @return a list of all hosts to bind to
     */
    Collection<ListenerInfo> getListeners();

    /**
     * Get all groups this player is in.
     *
     * @param player the player to check
     * @return all the player's groups.
     */
    Collection<String> getGroups(String player);

    /**
     * Get all permission corresponding to the specified group. The result of
     * this method may or may not be cached, depending on the implementation.
     *
     * @param group the group to check
     * @return all true permissions for this group
     */
    Collection<String> getPermissions(String group);
}
