package net.md_5.bungee.tablist;

import java.util.Collection;
import java.util.HashSet;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.TabListHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PacketC9PlayerListItem;

public class Global implements TabListHandler
{

    private final Collection<ProxiedPlayer> sentPings = new HashSet<>();

    @Override
    public void onConnect(ProxiedPlayer player)
    {
        for ( ProxiedPlayer p : ProxyServer.getInstance().getPlayers() )
        {
            player.unsafe().sendPacket( new PacketC9PlayerListItem( p.getDisplayName(), true, (short) p.getPing() ) );
        }
        BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( player.getDisplayName(), true, (short) player.getPing() ) );
    }

    @Override
    public void onPingChange(ProxiedPlayer player, int ping)
    {
        if ( !sentPings.contains( player ) )
        {
            BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( player.getDisplayName(), true, (short) player.getPing() ) );
            sentPings.add( player );
        }
    }

    @Override
    public void onDisconnect(ProxiedPlayer player)
    {
        BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( player.getDisplayName(), false, (short) 9999 ) );
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
