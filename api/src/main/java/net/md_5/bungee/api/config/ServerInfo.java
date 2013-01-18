package net.md_5.bungee.api.config;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Synchronized;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Class used to represent a server to connect to.
 */
@Data
@AllArgsConstructor
public class ServerInfo
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
     * Permission node required to access this server.
     */
    private String permission;
    /**
     * Players connected to a server defined by these properties.
     */
    private final Collection<ProxiedPlayer> players = new ArrayList<>();

    /**
     * Add a player to the internal set of this server.
     *
     * @param player the player to add
     */
    @Synchronized("players")
    public void addPlayer(ProxiedPlayer player)
    {
        players.add(player);
    }

    /**
     * Remove a player form the internal set of this server.
     *
     * @param player the player to remove
     */
    @Synchronized("players")
    public void removePlayer(ProxiedPlayer player)
    {
        players.remove(player);
    }

    /**
     * Get the set of all players on this server.
     *
     * @return an unmodifiable collection of all players on this server
     */
    @Synchronized("players")
    public Collection<ProxiedPlayer> getPlayers()
    {
        return Collections.unmodifiableCollection(players);
    }
}
