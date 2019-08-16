package net.md_5.bungee;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.boss.BarColor;
import net.md_5.bungee.api.boss.BarFlag;
import net.md_5.bungee.api.boss.BarStyle;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.BossBar;

@ToString
@EqualsAndHashCode
public class BungeeBossBar implements net.md_5.bungee.api.boss.BossBar
{

    @Getter
    private BaseComponent[] title;
    @Getter
    private BarColor color;
    @Getter
    private BarStyle style;
    @Getter
    private float progress;
    @Getter
    private boolean visible;

    private Set<ProxiedPlayer> players;
    private EnumSet<BarFlag> flags;

    @ToString.Exclude
    private final UUID uuid = UUID.randomUUID();
    @ToString.Exclude
    private final BossBar removePacket;

    public BungeeBossBar(BaseComponent[] title, BarColor color, BarStyle style, float progress)
    {
        this.title = Preconditions.checkNotNull( title, "title" );
        this.color = Preconditions.checkNotNull( color, "color" );
        this.style = Preconditions.checkNotNull( style, "style" );
        Preconditions.checkArgument( 0 <= progress && progress <= 1, "Progress may not be lower than 0 or greater than 1" );
        this.progress = progress;
        this.visible = true;
        this.players = new HashSet<>();
        this.flags = EnumSet.noneOf( BarFlag.class );
        this.removePacket = new BossBar( uuid, 1 );
    }

    @Override
    public boolean canBeAdded(ProxiedPlayer player)
    {
        return player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_9;
    }

    @Override
    public void addPlayer(ProxiedPlayer player)
    {
        Preconditions.checkNotNull( player, "player" );
        Preconditions.checkArgument( canBeAdded( player ),
                player.getName() + " cannot be added to BossBar ( make sure to use canBeAdded to avoid such errors )" );
        if ( !canBeAdded( player ) )
        {
            return;
        }
        players.add( player );
        if ( player.isConnected() && visible )
        {
            sendPacket( player, addPacket() );
        }
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
    public void removePlayer(ProxiedPlayer player)
    {
        Preconditions.checkNotNull( player, "player" );
        players.remove( player );
        if ( player.isConnected() && visible )
        {
            sendPacket( player, removePacket );
        }
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
        sendToAffected( removePacket );
        players.clear();
    }

    @Override
    public Collection<ProxiedPlayer> getPlayers()
    {
        return ImmutableList.copyOf( players );
    }

    @Override
    public void setTitle(BaseComponent[] title)
    {
        this.title = Preconditions.checkNotNull( title, "title" );
        if ( visible )
        {
            BossBar packet = new BossBar( uuid, 3 );
            packet.setTitle( ComponentSerializer.toString( title ) );
            sendToAffected( packet );
        }
    }

    @Override
    public void setProgress(float progress)
    {
        Preconditions.checkArgument( 0 <= this.progress && this.progress <= 1, "Progress may not be lower than 0 or greater than 1" );
        this.progress = progress;
        if ( visible )
        {
            BossBar packet = new BossBar( uuid, 2 );
            packet.setHealth( this.progress );
            sendToAffected( packet );
        }
    }

    @Override
    public void setColor(BarColor color)
    {
        this.color = Preconditions.checkNotNull( color, "color" );
        if ( visible )
        {
            setDivisions( color, style );
        }
    }

    @Override
    public void setStyle(BarStyle style)
    {
        this.style = Preconditions.checkNotNull( style, "style" );
        if ( visible )
        {
            setDivisions( color, style );
        }
    }

    @Override
    public void setVisible(boolean visible)
    {
        boolean previous = this.visible;
        if ( previous && !visible )
        {
            sendToAffected( removePacket );
        } else if ( !previous && visible )
        {
            sendToAffected( addPacket() );
        }
        this.visible = visible;
    }

    @Override
    public Collection<BarFlag> getFlags()
    {
        return ImmutableList.copyOf( flags );
    }

    @Override
    public void addFlags(BarFlag... flags)
    {
        if ( this.flags.addAll( Arrays.asList( flags ) ) && visible )
        {
            sendToAffected( updateFlags() );
        }
    }

    @Override
    public void removeFlag(BarFlag flag)
    {
        if ( flags.remove( flag ) && visible )
        {
            sendToAffected( updateFlags() );
        }
    }

    @Override
    public void removeFlags(BarFlag... flags)
    {
        if ( this.flags.removeAll( Arrays.asList( flags ) ) && visible )
        {
            sendToAffected( updateFlags() );
        }
    }

    private byte serializeFlags()
    {
        byte flagMask = 0x0;
        if ( flags.contains( BarFlag.DARKEN_SCREEN ) )
        {
            flagMask |= 0x1;
        }
        if ( flags.contains( BarFlag.PLAY_BOSS_MUSIC ) )
        {
            flagMask |= 0x2;
        }
        if ( flags.contains( BarFlag.CREATE_WORLD_FOG ) )
        {
            flagMask |= 0x4;
        }
        return flagMask;
    }

    private void setDivisions(BarColor color, BarStyle division)
    {
        BossBar packet = new BossBar( uuid, 4 );
        packet.setColor( color.ordinal() );
        packet.setDivision( division.ordinal() );
        sendToAffected( packet );
    }

    private BossBar updateFlags()
    {
        BossBar packet = new BossBar( uuid, 5 );
        packet.setFlags( serializeFlags() );
        return packet;
    }

    private BossBar addPacket()
    {
        BossBar packet = new BossBar( uuid, 0 );
        packet.setTitle( ComponentSerializer.toString( title ) );
        packet.setColor( color.ordinal() );
        packet.setDivision( style.ordinal() );
        packet.setHealth( progress );
        packet.setFlags( serializeFlags() );
        return packet;
    }

    private void sendToAffected(DefinedPacket packet)
    {
        for ( ProxiedPlayer player : players )
        {
            sendPacket( player, packet );
        }
    }

    private void sendPacket(ProxiedPlayer player, DefinedPacket packet)
    {
        if ( player.isConnected() )
        {
            player.unsafe().sendPacket( packet );
        }
    }
}
