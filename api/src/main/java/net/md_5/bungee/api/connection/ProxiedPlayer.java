package net.md_5.bungee.api.connection;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.Callback;
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
     * Connects / transfers this user to the specified connection, gracefully
     * closing the current one. Depending on the implementation, this method
     * might return before the user has been connected.
     *
     * @param target the new server to connect to
     * @param callback the method called when the connection is complete, or
     * when an exception is encountered. The boolean parameter denotes success
     * or failure.
     */
    void connect(ServerInfo target, Callback<Boolean> callback);

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
     * @deprecated In favour of {@link #getUniqueId()}
     */
    @Deprecated
    String getUUID();

    /**
     * Get this connection's UUID, if set.
     *
     * @return the UUID
     */
    UUID getUniqueId();

    /**
     * Gets this player's locale.
     *
     * @return the locale
     */
    Locale getLocale();

    /**
     * Gets this player's Forge Mod List, if the player has sent this information during the lifetime of their connection to Bungee.
     * There is no guarantee that information is available at any time, as it is only sent during a
     * FML handshake. Therefore, this will only contain information for a user that has attempted joined a Forge server.
     * <p>
     * Consumers of this API should be aware that an empty mod list does <em>not</em> indicate
     * that a user is not a Forge user, and so should not use this API to check for this - there is no way to tell this reliably.
     * </p>
     * <p>
     * Calling this when handling a {@link net.md_5.bungee.api.event.ServerConnectedEvent} may be the best place to do so
     * as this event occurs after a FML handshake has completed, if any has occurred.
     * </p>
     * 
     * @return A {@link Map} of mods, where the key is the name of the mod, and the value is the version.
     *         Returns an empty list if the FML handshake has not occurred for this {@link ProxiedPlayer} yet.
     */
    Map<String, String> getModList();
}
