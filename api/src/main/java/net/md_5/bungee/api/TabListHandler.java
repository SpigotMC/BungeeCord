package net.md_5.bungee.api;

import lombok.Data;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Data
public abstract class TabListHandler
{

    private final ProxiedPlayer player;

    /**
     * Called when this player first connects to the proxy.
     */
    public void onConnect()
    {
    }

    /**
     * Called when a player first connects to the proxy.
     *
     * @param player the connecting player
     */
    public void onServerChange()
    {
    }

    /**
     * Called when a players ping changes. The new ping will have not updated in
     * the player instance until this method returns.
     *
     * @param player the player who's ping changed
     * @param ping the player's new ping.
     */
    public void onPingChange(int ping)
    {
    }

    /**
     * Called when a player disconnects.
     *
     * @param player the disconnected player
     */
    public void onDisconnect()
    {
    }

    /**
     * Called when a list update packet is sent from server to client.
     *
     * @param player receiving this packet
     * @param name the player which this packet is relevant to
     * @param online whether the subject player is online
     * @param ping ping of the subject player
     * @return whether to send the packet to the client
     */
    public abstract boolean onListUpdate(String name, boolean online, int ping);
}
