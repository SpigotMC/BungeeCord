package net.md_5.bungee.tab;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.profile.ProfileProperty;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.packet.PlayerListItem;

@RequiredArgsConstructor
public abstract class TabList
{

    protected final ProxiedPlayer player;

    public abstract void onUpdate(PlayerListItem playerListItem);

    public abstract void onPingChange(int ping);

    public abstract void onServerChange();

    public abstract void onConnect();

    public abstract void onDisconnect();

    public static PlayerListItem rewrite(PlayerListItem playerListItem)
    {
        for ( PlayerListItem.Item item : playerListItem.getItems() )
        {
            if ( item.getUuid() == null ) // Old style ping
            {
                continue;
            }
            UserConnection player = BungeeCord.getInstance().getPlayerByOfflineUUID( item.getUuid() );
            if ( player != null )
            {
                item.setUuid( player.getUniqueId() );
                LoginResult loginResult = player.getPendingConnection().getLoginProfile();
                if ( loginResult.getProperties() != null && !loginResult.getProperties().isEmpty() )
                {
                    String[][] props = new String[ loginResult.getProperties().size() ][];
                    int index = 0;
                    for ( ProfileProperty property : loginResult.getProperties() )
                    {
                        props[index] = new String[]
                        {
                            property.getName(),
                            property.getValue(),
                            property.getSignature()
                        };
                        index++;
                    }
                    item.setProperties( props );
                } else
                {
                    item.setProperties( new String[ 0 ][ 0 ] );
                }
                if ( playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER || playerListItem.getAction() == PlayerListItem.Action.UPDATE_GAMEMODE )
                {
                    player.setGamemode( item.getGamemode() );
                }
                if ( playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER || playerListItem.getAction() == PlayerListItem.Action.UPDATE_LATENCY )
                {
                    player.setPing( item.getPing() );
                }
            }
        }
        return playerListItem;
    }
}
