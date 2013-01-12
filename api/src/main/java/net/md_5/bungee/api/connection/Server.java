package net.md_5.bungee.api.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;

/**
 * Represents a destination which this proxy might connect to.
 */
@RequiredArgsConstructor
public abstract class Server implements Connection
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
     * @param callback the callback to call when the count has been retrieved.
     */
    public abstract void getPlayerCount(Callback<ServerPing> callback);
}
