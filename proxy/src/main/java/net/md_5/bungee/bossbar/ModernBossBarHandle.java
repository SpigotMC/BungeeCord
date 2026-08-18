package net.md_5.bungee.bossbar;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.Protocol;

public class ModernBossBarHandle extends BossBarHandle
{

    public ModernBossBarHandle(UserConnection player, BungeeBossBar bungeeBossBar)
    {
        super( player, bungeeBossBar );
    }

    public void trySend()
    {
        if ( player.getCh().getEncodeProtocol() == Protocol.GAME )
        {
            sendPackets();
        }
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

    public void onServerSwitch()
    {
        tryRemove();
    }

    public void onServerConnected()
    {
        trySend();
    }

    public void onPlayerDisconnect()
    {
        bungeeBossBar.disconnectRemove( player );
    }

}
