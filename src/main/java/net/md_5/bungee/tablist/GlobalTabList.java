package net.md_5.bungee.tablist;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.packet.PacketC9PlayerListItem;

public class GlobalTabList implements TabListHandler
{

    private Set<UserConnection> sentPings = Collections.synchronizedSet(new HashSet<UserConnection>());

    @Override
    public void onJoin(UserConnection con)
    {
        for (UserConnection c : BungeeCord.instance.connections.values())
        {
            con.packetQueue.add(new PacketC9PlayerListItem(c.tabListName, true, c.getPing()));
        }
    }

    @Override
    public void onServerChange(UserConnection con)
    {
    }

    @Override
    public void onPingChange(final UserConnection con, final int ping)
    {
        if (!sentPings.contains(con))
        {
            BungeeCord.instance.broadcast(new PacketC9PlayerListItem(con.tabListName, true, con.getPing()));
            sentPings.add(con);
        }
    }

    @Override
    public void onDisconnect(final UserConnection con)
    {
        BungeeCord.instance.broadcast(new PacketC9PlayerListItem(con.tabListName, false, 9999));
        sentPings.remove(con);
    }

    @Override
    public boolean onPacketC9(UserConnection con, PacketC9PlayerListItem packet)
    {
        return false;
    }
}
