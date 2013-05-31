package net.md_5.bungee.tablist;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PacketC9PlayerListItem;

public class GlobalPing extends Global
{

    private static final int PING_THRESHOLD = 20;
    private final TObjectIntMap<ProxiedPlayer> lastPings = new TObjectIntHashMap<>();

    @Override
    public void onDisconnect(ProxiedPlayer player)
    {
        lastPings.remove( player );
        super.onDisconnect( player );
    }

    @Override
    public void onPingChange(ProxiedPlayer player, int ping)
    {
        int lastPing = lastPings.get( player );
        if ( ping - PING_THRESHOLD > lastPing && ping + PING_THRESHOLD < lastPing )
        {
            BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( player.getDisplayName(), true, (short) ping ) );
            lastPings.put( player, ping );
        }
    }
}
