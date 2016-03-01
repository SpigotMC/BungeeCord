package net.md_5.bungee.tab;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.PlayerListItem;

public class ServerUnique extends TabList
{

    private final Collection<UUID> uuids = new HashSet<>();

    public ServerUnique(ProxiedPlayer player)
    {
        super( player );
    }

    @Override
    public void onUpdate(PlayerListItem playerListItem)
    {
        for ( PlayerListItem.Item item : playerListItem.getItems() )
        {
            if ( playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER )
            {
                uuids.add( item.getUuid() );
            } else if ( playerListItem.getAction() == PlayerListItem.Action.REMOVE_PLAYER )
            {
                uuids.remove( item.getUuid() );
            }
        }
        player.unsafe().sendPacket( playerListItem );
    }

    @Override
    public void onPingChange(int ping)
    {

    }

    @Override
    public void onServerChange()
    {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction( PlayerListItem.Action.REMOVE_PLAYER );
        PlayerListItem.Item[] items = new PlayerListItem.Item[ uuids.size() ];
        int i = 0;
        for ( UUID uuid : uuids )
        {
            PlayerListItem.Item item = items[i++] = new PlayerListItem.Item();
            item.setUuid( uuid );
        }
        packet.setItems( items );
        player.unsafe().sendPacket( packet );
        uuids.clear();
    }

    @Override
    public void onConnect()
    {

    }

    @Override
    public void onDisconnect()
    {

    }
}
