package net.md_5.bungee.api.connection;

import java.net.InetSocketAddress;

/**
 * A proxy connection is defined as a connection directly connected to a socket.
 * It should expose information about the remote peer, however not be specific
 * to a type of connection, whether server or player.
 */
public interface Connection
{

    /**
     * Gets the remote address of this connection.
     *
     * @return the remote address
     */
    public InetSocketAddress getAddress();
}
