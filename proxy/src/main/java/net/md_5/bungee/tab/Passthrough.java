package net.md_5.bungee.tab;

import net.md_5.bungee.api.tab.TabListAdapter;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.Collection;
import java.util.HashSet;

public class Passthrough extends TabListAdapter
{
    @Override
    public boolean onListUpdate(String name, boolean online, int ping)
    {
        getPlayer().unsafe().sendPacket(new PlayerListItem(name, online, (short) ping));
        return true;
    }
}
