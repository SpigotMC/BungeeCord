package net.md_5.bungee.api.config;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Synchronized;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Class used to represent a server to connect to.
 */
@Data
@AllArgsConstructor
public abstract class ServerInfo
{

    /**
     * Name this server displays as.
     */
    private final String name;
    /**
     * Connectable address of this server.
     */
    private final InetSocketAddress address;
    /**
     * Players connected to a server defined by these properties.
     */
    private final Collection<ProxiedPlayer> players = new ArrayList<>();
    /**
     * If set to true, users will need special permissions to view this server.
     */
    private final boolean restricted;

    /**
     * Add a player to the internal set of this server.
     *
     * @param player the player to add
     */
    @Synchronized("players")
    public void addPlayer(ProxiedPlayer player)
    {
        players.add( player );
    }

    /**
     * Remove a player form the internal set of this server.
     *
     * @param player the player to remove
     */
    @Synchronized("players")
    public void removePlayer(ProxiedPlayer player)
    {
        players.remove( player );
    }

    /**
     * Get the set of all players on this server.
     *
     * @return an unmodifiable collection of all players on this server
     */
    @Synchronized("players")
    public Collection<ProxiedPlayer> getPlayers()
    {
        return Collections.unmodifiableCollection( players );
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
    public abstract void ping(Callback<ServerPing> callback);

    /**
     * Whether the player can access this server. It will only return false when
     * the player has no permission and this server is restricted.
     *
     * @param player the player to check access for
     * @return whether access is granted to this server
     */
    public boolean canAccess(ProxiedPlayer player)
    {
        return !restricted || player.hasPermission( "bungeecord.server." + name );
    }
}
