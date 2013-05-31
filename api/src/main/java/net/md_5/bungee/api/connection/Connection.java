package net.md_5.bungee.api.connection;

import java.net.InetSocketAddress;
import net.md_5.bungee.protocol.packet.DefinedPacket;

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
    InetSocketAddress getAddress();

    /**
     * Disconnects this end of the connection for the specified reason. If this
     * is an {@link ProxiedPlayer} the respective server connection will be
     * closed too.
     *
     * @param reason the reason shown to the player / sent to the server on
     * disconnect
     */
    void disconnect(String reason);

    /**
     * Get the unsafe methods of this class.
     *
     * @return the unsafe method interface
     */
    Unsafe unsafe();

    interface Unsafe
    {

        /**
         * Send a packet to this connection.
         *
         * @param packet the packet to send
         */
        void sendPacket(DefinedPacket packet);
    }
}
