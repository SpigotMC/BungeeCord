package net.md_5.bungee.tab;

import java.util.Collection;
import java.util.HashSet;
import net.md_5.bungee.api.tab.TabListAdapter;
import net.md_5.bungee.protocol.packet.PlayerListItem;

public class ServerUnique extends TabListAdapter
{

    private final Collection<String> usernames = new HashSet<>();

    @Override
    public void onServerChange()
    {
        synchronized ( usernames )
        {
            for ( String username : usernames )
            {
                getPlayer().unsafe().sendPacket( new PlayerListItem( username, false, (short) 9999 ) );
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
