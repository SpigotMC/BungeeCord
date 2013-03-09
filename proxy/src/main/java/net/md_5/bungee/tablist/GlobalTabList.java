package net.md_5.bungee.tablist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.TabListHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.packet.PacketC9PlayerListItem;

public class GlobalTabList implements TabListHandler
{

    // Sweet trick to avoid locking, basically reimplement HashSet based on a ConcurrentHashMap
    private static final Object PRESENT = new Object();
    private final Map<ProxiedPlayer, Object> sentPings = new ConcurrentHashMap<>();

    @Override
    public void onConnect(ProxiedPlayer player)
    {
        UserConnection con = (UserConnection) player;
        for ( ProxiedPlayer p : ProxyServer.getInstance().getPlayers() )
        {
            con.sendPacket(new PacketC9PlayerListItem( p.getDisplayName(), true, p.getPing() ) );
        }
        BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( player.getDisplayName(), true, player.getPing() ) );
    }

    @Override
    public void onPingChange(ProxiedPlayer player, int ping)
    {
        if ( !sentPings.containsKey( player ) )
        {
            BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( player.getDisplayName(), true, player.getPing() ) );
            sentPings.put( player, PRESENT );
        }
    }

    @Override
    public void onDisconnect(ProxiedPlayer player)
    {
        BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( player.getDisplayName(), false, 9999 ) );
        sentPings.remove( player );
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
