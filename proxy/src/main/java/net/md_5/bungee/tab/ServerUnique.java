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
    private final Collection<String> usernames = new HashSet<>(); // Support for <=1.7.9

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
                if ( item.getUuid() != null )
                {
                    uuids.add( item.getUuid() );
                } else
                {
                    usernames.add( item.getUsername() );
                }
            } else if ( playerListItem.getAction() == PlayerListItem.Action.REMOVE_PLAYER )
            {
                if ( item.getUuid() != null )
                {
                    uuids.remove( item.getUuid() );
                } else
                {
                    usernames.remove( item.getUsername() );
                }
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
        PlayerListItem.Item[] items = new PlayerListItem.Item[ uuids.size() + usernames.size() ];
        int i = 0;
        for ( UUID uuid : uuids )
        {
            PlayerListItem.Item item = items[i++] = new PlayerListItem.Item();
            item.setUuid( uuid );
        }
        for ( String username : usernames )
        {
            PlayerListItem.Item item = items[i++] = new PlayerListItem.Item();
            item.setUsername( username );
            item.setDisplayName( username );
        }
        packet.setItems( items );
        if ( player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_8 )
        {
            player.unsafe().sendPacket( packet );
        } else
        {
            // Split up the packet
            for ( PlayerListItem.Item item : packet.getItems() )
            {
                PlayerListItem p2 = new PlayerListItem();
                p2.setAction( packet.getAction() );

                p2.setItems( new PlayerListItem.Item[]
                {
                    item
                } );
                player.unsafe().sendPacket( p2 );
            }
        }
        uuids.clear();
        usernames.clear();
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
