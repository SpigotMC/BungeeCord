package net.md_5.bungee.tablist;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.TabListHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.packet.PacketC9PlayerListItem;

public class GlobalTabList implements TabListHandler
{

    private Set<ProxiedPlayer> sentPings = Collections.synchronizedSet(new HashSet<ProxiedPlayer>());

    @Override
    public void onConnect(ProxiedPlayer player)
    {
        for (UserConnection c : BungeeCord.instance.connections.values())
        {
            con.packetQueue.add(new PacketC9PlayerListItem(c.tabListName, true, c.getPing()));
        }
    }

    @Override
    public void onPingChange(ProxiedPlayer player, int ping)
    {
        if (!sentPings.contains(player))
        {
            BungeeCord.instance.broadcast(new PacketC9PlayerListItem(player.getDisplayName(), true, player.getPing()));
            sentPings.add(player);
        }
    }

    @Override
    public void onDisconnect(ProxiedPlayer player)
    {
        BungeeCord.instance.broadcast(new PacketC9PlayerListItem(player.getDisplayName(), false, 9999));
        sentPings.remove(player);
    }

    @Override
    public void onServerChange(ProxiedPlayer player)
    {
    }

    @Override
    public boolean onListUpdate(ProxiedPlayer player, String name, boolean online, int ping)
    {
        return false;
    }
}
