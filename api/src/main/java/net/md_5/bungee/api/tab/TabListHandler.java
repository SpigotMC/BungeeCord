package net.md_5.bungee.api.tab;

import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @deprecated Future Minecraft versions render this API useless
 */
@Deprecated
public interface TabListHandler
{

    /**
     * Called so that this class may set member fields to keep track of its
     * internal state. You should not do any packet sending or manipulation of
     * the passed player, other than storing it.
     *
     * @param player the player to be associated with this list
     */
    void init(ProxiedPlayer player);

    /**
     * Called when this player first connects to the proxy.
     */
    void onConnect();

    /**
     * Called when a player first connects to the proxy.
     */
    void onServerChange();

    /**
     * Called when a players ping changes. The new ping will have not updated in
     * the player instance until this method returns.
     *
     * @param ping the player's new ping.
     */
    void onPingChange(int ping);

    /**
     * Called when a player disconnects.
     */
    void onDisconnect();

    /**
     * Called when a list update packet is sent from server to client.
     *
     * @param name the player which this packet is relevant to
     * @param online whether the subject player is online
     * @param ping ping of the subject player
     * @return whether to send the packet to the client
     */
    boolean onListUpdate(String name, boolean online, int ping);
}
