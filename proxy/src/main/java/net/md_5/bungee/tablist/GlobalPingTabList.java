package net.md_5.bungee.tablist;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.packet.PacketC9PlayerListItem;

public class GlobalPingTabList extends GlobalTabList
{

    public static final int PING_THRESHOLD = 20;
    private Map<ProxiedPlayer, Integer> lastPings = Collections.synchronizedMap(new WeakHashMap<ProxiedPlayer, Integer>());

    @Override
    public void onPingChange(ProxiedPlayer player, int ping)
    {
        Integer lastPing = lastPings.get(player);
        if (lastPing == null || (ping - PING_THRESHOLD > lastPing && ping + PING_THRESHOLD < lastPing))
        {
            BungeeCord.instance.broadcast(new PacketC9PlayerListItem(player.getDisplayName(), true, ping));
            lastPings.put(player, ping);
        }
    }
}
