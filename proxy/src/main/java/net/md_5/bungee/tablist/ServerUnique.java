package net.md_5.bungee.tablist;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.TabListHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PacketC9PlayerListItem;

public class ServerUnique implements TabListHandler
{

    private final Multimap<ProxiedPlayer, String> sentUsernames = Multimaps.synchronizedMultimap( HashMultimap.<ProxiedPlayer, String>create() );

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
        sentUsernames.removeAll( player );
    }

    @Override
    public void onServerChange(ProxiedPlayer player)
    {
        Collection<String> usernames = sentUsernames.get( player );
        synchronized ( sentUsernames )
        {
            for ( String username : usernames )
            {
                player.unsafe().sendPacket( new PacketC9PlayerListItem( username, false, (short) 9999 ) );
            }
            usernames.clear();
        }
    }

    @Override
    public boolean onListUpdate(ProxiedPlayer player, String name, boolean online, int ping)
    {
        if ( online )
        {
            sentUsernames.put( player, name );
        } else
        {
            sentUsernames.remove( player, name );
        }

        return true;
    }
}
