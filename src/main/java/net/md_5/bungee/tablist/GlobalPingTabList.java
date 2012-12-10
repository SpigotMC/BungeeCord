package net.md_5.bungee.tablist;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.packet.PacketC9PlayerListItem;

public class GlobalPingTabList extends GlobalTabList
{

    public static final int PING_THRESHOLD = 20;
    private Map<UserConnection, Integer> lastPings = Collections.synchronizedMap(new WeakHashMap<UserConnection, Integer>());

    @Override
    public void onPingChange(final UserConnection con, final int ping)
    {
        Integer lastPing = lastPings.get(con);
        if (lastPing == null || (ping - PING_THRESHOLD > lastPing && ping + PING_THRESHOLD < lastPing))
        {
            BungeeCord.instance.broadcast(new PacketC9PlayerListItem(con.tabListName, true, ping));
            lastPings.put(con, ping);
        }
    }
}
