package net.md_5.bungee.api.connection;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.tab.TabListHandler;

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
    String getDisplayName();

    /**
     * Sets this players display name to be used as their nametag and tab list
     * name.
     *
     * @param name the name to set
     */
    void setDisplayName(String name);

    /**
     * Connects / transfers this user to the specified connection, gracefully
     * closing the current one. Depending on the implementation, this method
     * might return before the user has been connected.
     *
     * @param target the new server to connect to
     */
    void connect(ServerInfo target);

    /**
     * Gets the server this player is connected to.
     *
     * @return the server this player is connected to
     */
    Server getServer();

    /**
     * Gets the ping time between the proxy and this connection.
     *
     * @return the current ping time
     */
    int getPing();

    /**
     * Send a plugin message to this player.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     */
    void sendData(String channel, byte[] data);

    /**
     * Get the pending connection that belongs to this player.
     *
     * @return the pending connection that this player used
     */
    PendingConnection getPendingConnection();

    /**
     * Make this player chat (say something), to the server he is currently on.
     *
     * @param message the message to say
     */
    void chat(String message);

    /**
     * Sets the new tab list for the user. At this stage it is not advisable to
     * change after the user has logged in!
     *
     * @param list the new list
     */
    void setTabList(TabListHandler list);

    /**
     * Get the current tab list.
     *
     * @return the tab list in use by this user
     */
    TabListHandler getTabList();

    /**
     * Get the server which this player will be sent to next time the log in.
     *
     * @return the server, or null if default
     */
    ServerInfo getReconnectServer();

    /**
     * Set the server which this player will be sent to next time the log in.
     *
     * @param server the server to set
     */
    void setReconnectServer(ServerInfo server);

    /**
     * Get this connection's UUID, if set.
     *
     * @return the UUID
     */
    String getUUID();
}
