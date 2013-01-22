package net.md_5.bungee.api;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface TabListHandler
{

    /**
     * Called when a player first connects to the proxy.
     *
     * @param player the connecting player
     */
    public void onConnect(ProxiedPlayer player);

    /**
     * Called when a player changes their connected server.
     *
     * @param player the player who changed servers
     */
    public void onServerChange(ProxiedPlayer player);

    /**
     * Called when a players ping changes. The new ping will have not updated in
     * the player instance until this method returns.
     *
     * @param player the player who's ping changed
     * @param ping the player's new ping.
     */
    public void onPingChange(ProxiedPlayer player, int ping);

    /**
     * Called when a player disconnects.
     *
     * @param player the disconnected player
     */
    public void onDisconnect(ProxiedPlayer player);

    /**
     * Called when a list update packet is sent from server to client.
     *
     * @param player receiving this packet
     * @param name the player which this packet is relevant to
     * @param online whether the subject player is online
     * @param ping ping of the subject player
     * @return whether to send the packet to the client
     */
    public boolean onListUpdate(ProxiedPlayer player, String name, boolean online, int ping);
}
