package net.md_5.bungee.api.config;

import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class allows plugins to set their own configuration adapter to load
 * settings from a different place.
 */
public interface ConfigurationAdapter
{

    /**
     * Reload all the possible values, and if necessary cache them for
     * individual getting.
     */
    public void load();

    /**
     * Gets an integer from the specified path.
     *
     * @param path the path to retrieve the integer from
     * @param def the default value
     * @return the retrieved integer
     */
    public int getInt(@NotNull String path, int def);

    /**
     * Gets a string from the specified path.
     *
     * @param path the path to retrieve the string from.
     * @param def the default value
     * @return the retrieved string
     */
    @Nullable
    @Contract("!null, !null -> !null; !null, null -> _")
    public String getString(@NotNull String path, @Nullable String def);

    /**
     * Gets a boolean from the specified path.
     *
     * @param path the path to retrieve the boolean form.
     * @param def the default value
     * @return the retrieved boolean
     */
    public boolean getBoolean(@NotNull String path, boolean def);

    /**
     * Get a list from the specified path.
     *
     * @param path the path to retrieve the list form.
     * @param def the default value
     * @return the retrieved list
     */
    @Nullable
    @Contract("!null, !null -> !null; !null, null -> _")
    public Collection<?> getList(@NotNull String path, @Nullable Collection<?> def);

    /**
     * Get the configuration all servers which may be accessible via the proxy.
     *
     * @return all accessible servers, keyed by name
     */
    @NotNull
    public Map<String, ServerInfo> getServers();

    /**
     * Get information about all hosts to bind the proxy to.
     *
     * @return a list of all hosts to bind to
     */
    @NotNull
    public Collection<ListenerInfo> getListeners();

    /**
     * Get all groups this player is in.
     *
     * @param player the player to check
     * @return all the player's groups.
     */
    @NotNull
    public Collection<String> getGroups(@Nullable String player);

    /**
     * Get all permission corresponding to the specified group. The result of
     * this method may or may not be cached, depending on the implementation.
     *
     * @param group the group to check
     * @return all true permissions for this group, if group does not exist, collection is empty and unmodifiable
     */
    @NotNull
    public Collection<String> getPermissions(String group);
}
