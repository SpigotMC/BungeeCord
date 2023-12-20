package net.md_5.bungee.tab;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;

@RequiredArgsConstructor
public abstract class TabList
{

    protected final ProxiedPlayer player;

    public abstract void onUpdate(PlayerListItem playerListItem);

    public abstract void onUpdate(PlayerListItemRemove playerListItem);

    public abstract void onUpdate(PlayerListItemUpdate playerListItem);

    public abstract void onPingChange(int ping);

    public abstract void onServerChange();

    public abstract void onConnect();

    public abstract void onDisconnect();

    public static PlayerListItem rewrite(PlayerListItem playerListItem)
    {
        for ( PlayerListItem.Item item : playerListItem.getItems() )
        {
            rewrite( item );
        }
        return playerListItem;
    }

    public static PlayerListItemRemove rewrite(PlayerListItemRemove playerListItem)
    {
        for ( int i = 0; i < playerListItem.getUuids().length; i++ )
        {
            UserConnection player = BungeeCord.getInstance().getPlayerByOfflineUUID( playerListItem.getUuids()[i] );
            if ( player != null )
            {
                playerListItem.getUuids()[i] = player.getUniqueId();

            }
        }

        return playerListItem;
    }

    public static PlayerListItemUpdate rewrite(PlayerListItemUpdate playerListItem)
    {
        for ( PlayerListItem.Item item : playerListItem.getItems() )
        {
            rewrite( item );
        }
        return playerListItem;
    }

    private static void rewrite(PlayerListItem.Item item)
    {
        if ( item.getUuid() == null ) // Old style ping
        {
            return;
        }
        UserConnection player = BungeeCord.getInstance().getPlayerByOfflineUUID( item.getUuid() );
        if ( player != null )
        {
            item.setUuid( player.getUniqueId() );

            if ( item.getProperties() != null )
            {
                LoginResult loginResult = player.getPendingConnection().getLoginProfile();
                if ( loginResult != null && loginResult.getProperties() != null )
                {
                    Property[] props = new Property[ loginResult.getProperties().length ];
                    for ( int i = 0; i < props.length; i++ )
                    {
                        props[i] = new Property( loginResult.getProperties()[i].getName(), loginResult.getProperties()[i].getValue(), loginResult.getProperties()[i].getSignature() );
                    }
                    item.setProperties( props );
                } else
                {
                    item.setProperties( new Property[ 0 ] );
                }
            }
            if ( item.getGamemode() != null )
            {
                player.setGamemode( item.getGamemode() );
            }
            if ( item.getPing() != null )
            {
                player.setPing( item.getPing() );
            }
        }
    }
}
