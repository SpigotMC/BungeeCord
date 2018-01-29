package net.md_5.bungee.api.connection;

import java.net.InetSocketAddress;
import java.util.UUID;
import net.md_5.bungee.api.config.ListenerInfo;

/**
 * Represents a user attempting to log into the proxy.
 */
public interface PendingConnection extends Connection
{

    /**
     * Get the requested username.
     *
     * @return the requested username, or null if not set
     */
    String getName();

    /**
     * Get the numerical client version of the player attempting to log in.
     *
     * @return the protocol version of the remote client
     */
    int getVersion();

    /**
     * Get the requested virtual host that the client tried to connect to.
     *
     * @return request virtual host or null if invalid / not specified.
     */
    InetSocketAddress getVirtualHost();

    /**
     * Get the listener that accepted this connection.
     *
     * @return the accepting listener
     */
    ListenerInfo getListener();

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
     * Set the connection's uuid
     *
     * @param uuid connection UUID
     */
    void setUniqueId(UUID uuid);

    /**
     * Get this connection's online mode.
     * <br>
     * See {@link #setOnlineMode(boolean)} for a description of how this option
     * works.
     *
     * @return the online mode
     */
    boolean isOnlineMode();

    /**
     * Set this connection's online mode.
     * <br>
     * May be called only during the PlayerHandshakeEvent to set the online mode
     * configuration setting for this connection only (i.e. whether or not the
     * client will be treated as if it is connecting to an online mode server).
     *
     * @param onlineMode status
     */
    void setOnlineMode(boolean onlineMode);

    /**
     * Check if the client is using the older unsupported Minecraft protocol
     * used by Minecraft clients older than 1.7.
     *
     * @return Whether the client is using a legacy client.
     */
    boolean isLegacy();
}
