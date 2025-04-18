package net.md_5.bungee.bossbar;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.boss.BarColor;
import net.md_5.bungee.api.boss.BarFlag;
import net.md_5.bungee.api.boss.BarStyle;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.BossBar;

@ToString
@EqualsAndHashCode(callSuper = true)
public class BungeeBossBar extends BossBarMeta implements net.md_5.bungee.api.boss.BossBar
{
    @Getter
    private final UUID uuid = UUID.randomUUID();
    private final Map<UserConnection, BossBarHandle> players = Collections.synchronizedMap( new IdentityHashMap<>() );

    @Getter
    @ToString.Exclude
    private final BossBar removePacket = new BossBar( uuid, 1 );

    public BungeeBossBar(BaseComponent title, BarColor color, BarStyle style, float progress)
    {
        super( Preconditions.checkNotNull( title, "title" ),
                Preconditions.checkNotNull( color, "color" ),
                Preconditions.checkNotNull( style, "style" ),
                progress, EnumSet.noneOf( BarFlag.class ), true );
        Preconditions.checkArgument( 0 <= progress && progress <= 1, "Progress may not be lower than 0 or greater than 1" );
    }

    @Override
    public boolean canBeAdded(ProxiedPlayer player)
    {
        return player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_9;
    }

    @Override
    public boolean addPlayer(ProxiedPlayer player)
    {
        Preconditions.checkNotNull( player, "player" );
        UserConnection connection = (UserConnection) player;
        if ( !canBeAdded( player ) || !player.isConnected() || players.containsKey( connection ) )
        {
            return false;
        }
        BossBarHandle handle = new BossBarHandle( connection, this );
        players.put( connection, handle );
        connection.getBungeeBossBarHandles().add( handle );
        return true;
    }

    @Override
    public void addPlayers(Iterable<ProxiedPlayer> players)
    {
        Preconditions.checkNotNull( players, "players" );
        for ( ProxiedPlayer player : players )
        {
            addPlayer( player );
        }
    }

    @Override
    public boolean removePlayer(ProxiedPlayer player)
    {
        Preconditions.checkNotNull( player, "player" );
        UserConnection connection = (UserConnection) player;
        if ( !players.containsKey( player ) )
        {
            return false;
        }
        BossBarHandle handle = players.remove( connection );
        connection.getBungeeBossBarHandles().remove( handle );
        handle.tryRemove();
        return true;
    }

    public void disconnectRemove(UserConnection userConnection)
    {
        players.remove( userConnection );
    }

    @Override
    public void removePlayers(Iterable<ProxiedPlayer> players)
    {
        Preconditions.checkNotNull( players, "players" );
        for ( ProxiedPlayer player : players )
        {
            removePlayer( player );
        }
    }

    @Override
    public void removeAllPlayers()
    {
        players.forEach( (player, handle) ->
        {
            player.getBungeeBossBarHandles().remove( handle );
            handle.tryRemove();
        } );
        players.clear();
    }

    @Override
    public Collection<ProxiedPlayer> getPlayers()
    {
        return ImmutableList.copyOf( players.keySet() );
    }

    @Override
    public void setTitle(BaseComponent title)
    {
        super.setTitle( title );
        publishUpdate();
    }

    @Override
    public void setColor(BarColor color)
    {
        super.setColor( color );
        publishUpdate();
    }

    @Override
    public void setFlags(EnumSet<BarFlag> flags)
    {
        super.setFlags( flags );
        publishUpdate();
    }

    @Override
    public void setProgress(float progress)
    {
        super.setProgress( progress );
        publishUpdate();
    }

    @Override
    public void setStyle(BarStyle style)
    {
        super.setStyle( style );
        publishUpdate();
    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible( visible );
        publishUpdate();
    }

    private void publishUpdate()
    {
        players.values().forEach( BossBarHandle::trySend );
    }
}
