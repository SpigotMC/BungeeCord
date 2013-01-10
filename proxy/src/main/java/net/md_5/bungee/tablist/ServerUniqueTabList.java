package net.md_5.bungee.tablist;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.packet.PacketC9PlayerListItem;

public class ServerUniqueTabList implements TabListHandler
{

    private Map<UserConnection, Set<String>> sentUsernames = Collections.synchronizedMap(new WeakHashMap<UserConnection, Set<String>>());

    @Override
    public void onJoin(UserConnection con)
    {
    }

    @Override
    public void onServerChange(UserConnection con)
    {
        Set<String> usernames = sentUsernames.get(con);
        if (usernames != null)
        {
            synchronized (usernames)
            {
                for (String username : usernames)
                {
                    con.packetQueue.add(new PacketC9PlayerListItem(username, false, 9999));
                }
                usernames.clear();
            }
        }
    }

    @Override
    public void onPingChange(UserConnection con, int ping)
    {
    }

    @Override
    public void onDisconnect(UserConnection con)
    {
    }

    @Override
    public boolean onPacketC9(final UserConnection con, final PacketC9PlayerListItem packet)
    {
        Set<String> usernames = sentUsernames.get(con);
        if (usernames == null)
        {
            usernames = new LinkedHashSet<>();
            sentUsernames.put(con, usernames);
        }

        if (packet.online)
        {
            usernames.add(packet.username);
        } else
        {
            usernames.remove(packet.username);
        }

        return true;
    }
}
