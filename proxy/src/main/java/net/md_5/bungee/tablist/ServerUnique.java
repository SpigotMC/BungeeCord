package net.md_5.bungee.tablist;

import java.util.Collection;
import java.util.HashSet;
import net.md_5.bungee.api.TabListHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PacketC9PlayerListItem;

public class ServerUnique extends TabListHandler
{

    private final Collection<String> usernames = new HashSet<>();

    public ServerUnique(ProxiedPlayer player)
    {
        super( player );
    }

    @Override
    public void onServerChange()
    {
        synchronized ( usernames )
        {
            for ( String username : usernames )
            {
                getPlayer().unsafe().sendPacket( new PacketC9PlayerListItem( username, false, (short) 9999 ) );
            }
            usernames.clear();
        }
    }

    @Override
    public boolean onListUpdate(String name, boolean online, int ping)
    {
        if ( online )
        {
            usernames.add( name );
        } else
        {
            usernames.remove( name );
        }

        return true;
    }
}
