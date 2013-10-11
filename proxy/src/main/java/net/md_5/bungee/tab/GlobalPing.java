package net.md_5.bungee.tab;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;

public class GlobalPing extends Global
{

    private static final int PING_THRESHOLD = 20;
    /*========================================================================*/
    private int lastPing;

    @Override
    public void onPingChange(int ping)
    {
        if ( ping - PING_THRESHOLD > lastPing && ping + PING_THRESHOLD < lastPing )
        {
            lastPing = ping;
            BungeeCord.getInstance().broadcast( new PlayerListItem( getPlayer().getDisplayName(), true, (short) ping ) );
        }
    }
}
