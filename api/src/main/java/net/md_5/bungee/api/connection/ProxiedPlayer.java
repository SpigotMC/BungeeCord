package net.md_5.bungee.api.connection;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.CommandSender;

/**
 * Represents a player who's connection is being connected to somewhere else,
 * whether it be a remote or embedded server.
 */
public abstract class ProxiedPlayer implements Connection, CommandSender
{

    /**
     * Name displayed to other users in areas such as the tab list.
     */
    @Getter
    @Setter
    private String displayName;

    /**
     * Connects / transfers this user to the specified connection, gracefully
     * closing the current one. Depending on the implementation, this method
     * might return before the user has been connected.
     *
     * @param server the new server to connect to
     */
    public abstract void connect(Server server);

    /**
     * Gets the ping time between the proxy and this connection.
     *
     * @return the current ping time
     */
    public abstract int getPing();

    /**
     * Completely kick this user from the proxy and all of its child
     * connections.
     *
     * @param reason the disconnect reason displayed to the player
     */
    public abstract void disconnect(String reason);

    /**
     * Send a plugin message to this player.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     */
    public abstract void sendData(String channel, byte[] data);
}
