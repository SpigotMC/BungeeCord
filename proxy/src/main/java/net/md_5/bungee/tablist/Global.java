package net.md_5.bungee.tablist;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.TabListHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PacketC9PlayerListItem;

public class Global extends TabListHandler
{

    private boolean sentPing;

    public Global(ProxiedPlayer player)
    {
        super( player );
    }

    @Override
    public void onConnect()
    {
        for ( ProxiedPlayer p : ProxyServer.getInstance().getPlayers() )
        {
            getPlayer().unsafe().sendPacket( new PacketC9PlayerListItem( p.getDisplayName(), true, (short) p.getPing() ) );
        }
        BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( getPlayer().getDisplayName(), true, (short) getPlayer().getPing() ) );
    }

    @Override
    public void onPingChange(int ping)
    {
        if ( !sentPing )
        {
            sentPing = true;
            BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( getPlayer().getDisplayName(), true, (short) getPlayer().getPing() ) );
        }
    }

    @Override
    public void onDisconnect()
    {
        BungeeCord.getInstance().broadcast( new PacketC9PlayerListItem( getPlayer().getDisplayName(), false, (short) 9999 ) );
    }

    @Override
    public boolean onListUpdate(String name, boolean online, int ping)
    {
        return false;
    }
}
