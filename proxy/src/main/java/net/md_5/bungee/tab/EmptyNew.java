package net.md_5.bungee.tab;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class EmptyNew extends TabList
{

    private final Collection<UUID> uuids = new HashSet<>();

    //This empty TabList is set for players with 1.8
    public EmptyNew(ProxiedPlayer player)
    {
        super( player );
    }

    @Override
    public void onUpdate(PlayerListItem playerListItem)
    {
        PlayerListItem.Item[] items = new PlayerListItem.Item[ playerListItem.getItems().length + 80 ];
        if( playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER )
        {
            for ( int i=0; i<80; i++ )
            {
                PlayerListItem.Item item = new PlayerListItem.Item();
                item.setUuid( UUID.randomUUID() );
                item.setUsername( " §" + (i / 10) + "§" + (i % 10) );
                item.setGamemode( 0 );
                item.setPing( 1000 );
                String[] textures = getTexture( player );
                item.setProperties( new String[][]{{"textures", textures[0],
                        textures[1]
                }} );
                items[ i ] = item;
            }
        }

        for ( int i = 0; i < playerListItem.getItems().length; i++  )
        {
            PlayerListItem.Item item = playerListItem.getItems()[i];
            if ( playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER )
            {
                if ( item.getUuid() != null )
                {
                    uuids.add(item.getUuid());
                    items[ i + 80 ] = item;
                }
            } else if ( playerListItem.getAction() == PlayerListItem.Action.REMOVE_PLAYER )
            {
                if ( item.getUuid() != null )
                {
                    uuids.remove( item.getUuid() );
                }
            }
        }

        if( playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER )
        {
            playerListItem.setItems( items );
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
//        PlayerListItem playerListItem = new PlayerListItem();
//        PlayerListItem.Item[] items = new PlayerListItem.Item[ 80 ];
//
//        for ( int i=0; i<80; i++ )
//        {
//            PlayerListItem.Item item = new PlayerListItem.Item();
//            item.setUuid( UUID.randomUUID() );
//            item.setUsername( "§" + (i / 10) + "§" + (i % 10));
//            item.setDisplayName( "§" + (i / 10) + "§" + (i % 10) );
//            item.setGamemode( 0 );
//            item.setPing( 1000 );
//            item.setProperties( new String[0][0] );
//            items[i] = item;
//        }
//
//        playerListItem.setAction( PlayerListItem.Action.ADD_PLAYER );
//        playerListItem.setItems( items );
//        player.unsafe().sendPacket( playerListItem );
    }

    @Override
    public void onDisconnect()
    {

    }

    static String[] getTexture( ProxiedPlayer player )
    {
        LoginResult loginResult = ((UserConnection) player).
                getPendingConnection().getLoginProfile();
        if (loginResult == null) {
            return null;
        }
        for (LoginResult.Property s : loginResult.getProperties()) {
            if (s.getName().equals("textures")) {
                return new String[]{s.getValue(), s.getSignature()};
            }
        }
        return null;
    }
}
