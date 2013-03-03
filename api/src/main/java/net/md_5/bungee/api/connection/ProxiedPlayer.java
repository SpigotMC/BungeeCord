package net.md_5.bungee.api.connection;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Represents a player who's connection is being connected to somewhere else,
 * whether it be a remote or embedded server.
 */
public interface ProxiedPlayer extends Connection, CommandSender
{

    /**
     * Gets this player's display name.
     *
     * @return the players current display name
     */
    public String getDisplayName();

    /**
     * Sets this players display name to be used as their nametag and tab list
     * name.
     *
     * @param name the name to set
     */
    public void setDisplayName(String name);

    /**
     * Connects / transfers this user to the specified connection, gracefully
     * closing the current one. Depending on the implementation, this method
     * might return before the user has been connected.
     *
     * @param target the new server to connect to
     */
    public void connect(ServerInfo target);

    /**
     * Gets the server this player is connected to.
     *
     * @return the server this player is connected to
     */
    public Server getServer();

    /**
     * Gets the ping time between the proxy and this connection.
     *
     * @return the current ping time
     */
    public int getPing();

    /**
     * Disconnect (remove) this player from the proxy with the specified reason.
     *
     * @param reason the reason displayed to the player
     */
    public void disconnect(String reason);

    /**
     * Send a plugin message to this player.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     */
    public void sendData(String channel, byte[] data);

    /**
     * Get the pending connection that belongs to this player.
     *
     * @return the pending connection that this player used
     */
    public PendingConnection getPendingConnection();
    
    /**
     * Send a chat message to the server the player is connected to.
     *
     * @param message the message to send
     */
    public void chat(String message);
}
