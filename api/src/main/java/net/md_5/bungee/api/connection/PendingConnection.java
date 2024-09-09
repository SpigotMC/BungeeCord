package net.md_5.bungee.api.connection;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.md_5.bungee.api.config.ListenerInfo;
import org.jetbrains.annotations.ApiStatus;

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

    /**
     * Gets if this connection has been transferred from another server.
     *
     * @return true if the connection has been transferred
     */
    @ApiStatus.Experimental
    boolean isTransferred();

    /**
     * Retrieves a cookie from this pending connection.
     *
     * @param cookie the resource location of the cookie, for example
     * "bungeecord:my_cookie"
     * @return a {@link CompletableFuture} that will be completed when the
     * Cookie response is received. If the cookie is not set in the client, the
     * {@link CompletableFuture} will complete with a null value
     * @throws IllegalStateException if the player's version is not at least
     * 1.20.5
     */
    @ApiStatus.Experimental
    CompletableFuture<byte[]> retrieveCookie(String cookie);

    /**
     * Sends a login payload request to the client.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     * @return a {@link CompletableFuture} that will be completed when the Login
     * Payload response is received. If the Vanilla client doesn't know the
     * channel, the {@link CompletableFuture} will complete with a null value
     * @throws IllegalStateException if the player's version is not at least
     * 1.13
     */
    @ApiStatus.Experimental
    CompletableFuture<byte[]> sendData(String channel, byte[] data);
}
