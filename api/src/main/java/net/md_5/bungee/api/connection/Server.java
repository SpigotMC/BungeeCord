package net.md_5.bungee.api.connection;

import net.md_5.bungee.api.config.ServerInfo;

/**
 * Represents a destination which this proxy might connect to.
 */
public interface Server extends Connection
{

    /**
     * Returns the basic information about this server.
     *
     * @return the {@link ServerInfo} for this server
     */
    public ServerInfo getInfo();

    /**
     * Send data by any available means to this server.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     */
    public abstract void sendData(String channel, byte[] data);
}
