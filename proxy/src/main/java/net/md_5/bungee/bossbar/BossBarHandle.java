package net.md_5.bungee.bossbar;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.BossBar;

public abstract class BossBarHandle
{
    protected final UserConnection player;
    protected final BungeeBossBar bungeeBossBar;
    protected BossBarMeta lastSendMeta;

    public BossBarHandle(UserConnection player, BungeeBossBar bungeeBossBar)
    {
        this.player = player;
        this.bungeeBossBar = bungeeBossBar;
        trySend();
    }

    public abstract void onServerSwitch();

    public abstract void onServerConnected();

    public abstract void onPlayerDisconnect();

    abstract void tryRemove();

    abstract void trySend();

    static BossBarHandle forPlayer(UserConnection player, BungeeBossBar bar)
    {
        if ( player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_20_2 )
        {
            return new ModernBossBarHandle( player, bar );
        } else
        {
            return new LegacyBossBarHandle( player, bar );
        }
    }

    protected BossBar addPacket()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 0 );
        packet.setTitle( bungeeBossBar.getTitle() );
        packet.setColor( bungeeBossBar.getColor().ordinal() );
        packet.setDivision( bungeeBossBar.getStyle().ordinal() );
        packet.setHealth( bungeeBossBar.getProgress() );
        packet.setFlags( bungeeBossBar.serializeFlags() );
        return packet;
    }

    protected BossBar updateProgressPacket()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 2 );
        packet.setHealth( bungeeBossBar.getProgress() );
        return packet;
    }

    protected BossBar updateTitlePacket()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 3 );
        packet.setTitle( bungeeBossBar.getTitle() );
        return packet;
    }

    protected BossBar updateColorAndDivision()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 4 );
        packet.setColor( bungeeBossBar.getColor().ordinal() );
        packet.setDivision( bungeeBossBar.getStyle().ordinal() );
        return packet;
    }

    protected BossBar updateFlagsPacket()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 5 );
        packet.setFlags( bungeeBossBar.serializeFlags() );
        return packet;
    }

    protected void sendPackets()
    {
        if ( lastSendMeta == null )
        {
            lastSendMeta = bungeeBossBar.duplicate();
            player.unsafe().sendPacket( addPacket() );
        } else
        {

            if ( !bungeeBossBar.isVisible() )
            {
                player.unsafe().sendPacket( bungeeBossBar.getRemovePacket() );
                lastSendMeta = null;
                return;
            }

            if ( !lastSendMeta.getFlags().equals( bungeeBossBar.getFlags() ) )
            {
                player.unsafe().sendPacket( updateFlagsPacket() );
            }

            if ( !lastSendMeta.getTitle().equals( bungeeBossBar.getTitle() ) )
            {
                player.unsafe().sendPacket( updateTitlePacket() );
            }

            if ( lastSendMeta.getProgress() != bungeeBossBar.getProgress() )
            {
                player.unsafe().sendPacket( updateProgressPacket() );
            }

            if ( lastSendMeta.getColor() != bungeeBossBar.getColor() || lastSendMeta.getStyle() != bungeeBossBar.getStyle() )
            {
                player.unsafe().sendPacket( updateColorAndDivision() );
            }
        }
    }
}
