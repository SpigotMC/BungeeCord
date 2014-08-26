package net.md_5.bungee.api.config;

import java.net.InetSocketAddress;
import java.util.Collection;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Class used to represent a server to connect to.
 */
public interface ServerInfo
{

    /**
     * Get the name of this server.
     *
     * @return the configured name for this server address
     */
    String getName();

    /**
     * Gets the connectable host + port pair for this server. Implementations
     * expect this to be used as the unique identifier per each instance of this
     * class.
     *
     * @return the IP and port pair for this server
     */
    InetSocketAddress getAddress();

    /**
     * Get the set of all players on this server.
     *
     * @return an unmodifiable collection of all players on this server
     */
    Collection<ProxiedPlayer> getPlayers();

    /**
     * Returns the MOTD which should be used when this server is a forced host.
     *
     * @return the motd
     */
    String getMotd();

    /**
     * Whether the player can access this server. It will only return false when
     * the player has no permission and this server is restricted.
     *
     * @param sender the player to check access for
     * @return whether access is granted to this server
     */
    boolean canAccess(CommandSender sender);

    /**
     * Send data by any available means to this server. This data may be queued
     * and there is no guarantee of its timely arrival.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     */
    void sendData(String channel, byte[] data);

    /**
     * Send data by any available means to this server.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     * @param queue hold the message for later sending if it cannot be sent
     * immediately.
     * @return <code>true</code> if the message was sent immediately,
     * <code>false</code> otherwise if queue is true, it has been queued, if it
     * is false it has been discarded.
     */
    boolean sendData(String channel, byte[] data, boolean queue);

    /**
     * Asynchronously gets the current player count on this server.
     *
     * @param callback the callback to call when the count has been retrieved.
     */
    void ping(Callback<ServerPing> callback);
}
