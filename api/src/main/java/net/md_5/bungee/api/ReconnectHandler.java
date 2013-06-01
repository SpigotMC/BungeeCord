package net.md_5.bungee.api;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface ReconnectHandler
{

    /**
     * Gets the initial server name for a connecting player.
     *
     * @param player the connecting player
     * @return the server to connect to
     */
    ServerInfo getServer(ProxiedPlayer player);

    /**
     * Save the server of this player before they disconnect so it can be
     * retrieved later.
     *
     * @param player the player to save
     */
    void setServer(ProxiedPlayer player); // TOOD: String + String arguments?

    /**
     * Save all pending reconnect locations. Whilst not used for database
     * connections, this method will be called at a predefined interval to allow
     * the saving of reconnect files.
     */
    void save();

    /**
     * Close all connections indicating that the proxy is about to shutdown and
     * all data should be saved. No new requests will be made after this method
     * has been called.
     *
     */
    void close();
}
