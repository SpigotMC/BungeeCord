package net.md_5.bungee.api.connection;

import java.net.InetSocketAddress;
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
    public String getName();

    /**
     * Get the numerical client version of the player attempting to log in.
     *
     * @return the protocol version of the remote client
     */
    public byte getVersion();

    /**
     * Get the requested virtual host that the client tried to connect to.
     *
     * @return request virtual host or null if invalid / not specified.
     */
    public InetSocketAddress getVirtualHost();

    /**
     * Get the listener that accepted this connection.
     *
     * @return the accepting listener
     */
    public ListenerInfo getListener();
}
