package net.md_5.bungee.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a destination which this proxy might connect to.
 */
@RequiredArgsConstructor
public abstract class RemoteServer extends ProxyConnection
{

    @Getter
    private final String name;

    /**
     * Send data by any available means to this server.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     */
    public abstract void sendData(String channel, byte[] data);

    /**
     * Asynchronously gets the current player count on this server.
     *
     * TODO: Return all info available via the standard query protocol
     *
     * @param callback the callback to call when the count has been retrieved.
     */
    public abstract void getPlayerCount(Callback<Integer> callback);
}
