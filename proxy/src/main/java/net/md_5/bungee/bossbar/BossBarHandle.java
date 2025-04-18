package net.md_5.bungee.bossbar;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.BossBar;

public class BossBarHandle
{
    private final UserConnection player;
    private final BungeeBossBar bungeeBossBar;
    private BossBarMeta lastSendMeta;

    public BossBarHandle(UserConnection player, BungeeBossBar bungeeBossBar)
    {
        this.player = player;
        this.bungeeBossBar = bungeeBossBar;
        trySend();
    }

    public void trySend()
    {
        if ( player.getCh().getEncodeProtocol() == Protocol.GAME )
        {
            sendPackets();
        }
    }

    public void onServerSwitch()
    {
        tryRemove();
    }

    public void tryRemove()
    {
        // only remove in game otherwise it is already removed
        if ( player.getCh().getEncodeProtocol() == Protocol.GAME )
        {
            if ( lastSendMeta != null && lastSendMeta.isVisible() )
            {
                player.unsafe().sendPacket( bungeeBossBar.getRemovePacket() );
            }
        }
        lastSendMeta = null;
    }

    public void onServerConnected()
    {
        trySend();
    }

    public void onPlayerDisconnect()
    {
        bungeeBossBar.disconnectRemove( player );
    }

    private void sendPackets()
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

    private BossBar addPacket()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 0 );
        packet.setTitle( bungeeBossBar.getTitle() );
        packet.setColor( bungeeBossBar.getColor().ordinal() );
        packet.setDivision( bungeeBossBar.getStyle().ordinal() );
        packet.setHealth( bungeeBossBar.getProgress() );
        packet.setFlags( bungeeBossBar.serializeFlags() );
        return packet;
    }

    private BossBar updateProgressPacket()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 2 );
        packet.setHealth( bungeeBossBar.getProgress() );
        return packet;
    }

    private BossBar updateTitlePacket()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 3 );
        packet.setTitle( bungeeBossBar.getTitle() );
        return packet;
    }

    private BossBar updateColorAndDivision()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 4 );
        packet.setColor( bungeeBossBar.getColor().ordinal() );
        packet.setDivision( bungeeBossBar.getStyle().ordinal() );
        return packet;
    }

    private BossBar updateFlagsPacket()
    {
        BossBar packet = new BossBar( bungeeBossBar.getUuid(), 5 );
        packet.setFlags( bungeeBossBar.serializeFlags() );
        return packet;
    }
}
