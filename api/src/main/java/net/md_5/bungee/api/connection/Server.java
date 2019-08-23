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
     * In recent Minecraft versions channel names must contain a colon separator
     * and consist of [a-z0-9/._-]. This will be enforced in a future version.
     * The "BungeeCord" channel is an exception and may only take this form.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     */
    public abstract void sendData(String channel, byte[] data);
}
