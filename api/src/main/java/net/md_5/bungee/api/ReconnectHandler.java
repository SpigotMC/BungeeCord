package net.md_5.bungee.api;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface ReconnectHandler
{

    /**
     * Gets the initial server name for a connecting player.
     *
     * @param player the connecting player
     * @return the server name
     */
    public String getServer(ProxiedPlayer player);

    /**
     * Save all pending reconnect locations. Whilst not used for database
     * connections, this method will be called at a predefined interval to allow
     * the saving of reconnect files.
     */
    public void save();
}
