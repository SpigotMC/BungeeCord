package net.md_5.bungee.api.connection;

import java.net.InetSocketAddress;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.ServerPing;

/**
 * Represents a destination which this proxy might connect to.
 */
@RequiredArgsConstructor
public abstract class Server implements Connection
{

    /**
     * Information about the address, name and configuration regarding this
     * server.
     */
    @Getter
    private final ServerInfo info;

    @Override
    public InetSocketAddress getAddress()
    {
        return info.getAddress();
    }

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
