package net.md_5.bungee.tablist;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.md_5.bungee.api.TabListHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.packet.PacketC9PlayerListItem;

public class ServerUniqueTabList implements TabListHandler
{

    private Map<ProxiedPlayer, Set<String>> sentUsernames = Collections.synchronizedMap(new WeakHashMap<ProxiedPlayer, Set<String>>());

    @Override
    public void onConnect(ProxiedPlayer player)
    {
    }

    @Override
    public void onPingChange(ProxiedPlayer player, int ping)
    {
    }

    @Override
    public void onDisconnect(ProxiedPlayer player)
    {
    }

    @Override
    public void onServerChange(ProxiedPlayer player)
    {
        Set<String> usernames = sentUsernames.get(player);
        if (usernames != null)
        {
            synchronized (usernames)
            {
                for (String username : usernames)
                {
                    player.packetQueue.add(new PacketC9PlayerListItem(username, false, 9999));
                }
                usernames.clear();
            }
        }
    }

    @Override
    public boolean onListUpdate(ProxiedPlayer player, String name, boolean online, int ping)
    {
        Set<String> usernames = sentUsernames.get(player);
        if (usernames == null)
        {
            usernames = new LinkedHashSet<>();
            sentUsernames.put(player, usernames);
        }

        if (online)
        {
            usernames.add(name);
        } else
        {
            usernames.remove(name);
        }

        return true;
    }
}
