package net.md_5.bungee.bossbar;

import net.md_5.bungee.UserConnection;

public class LegacyBossBarHandle extends BossBarHandle
{

    public LegacyBossBarHandle(UserConnection player, BungeeBossBar bungeeBossBar)
    {
        super( player, bungeeBossBar );
    }

    public void trySend()
    {
        sendPackets();
    }

    public void tryRemove()
    {
        // only remove in game otherwise it is already removed
        if ( lastSendMeta != null && lastSendMeta.isVisible() )
        {
            player.unsafe().sendPacket( bungeeBossBar.getRemovePacket() );
        }
        lastSendMeta = null;
    }

    public void onServerConnected()
    {
    }

    public void onPlayerDisconnect()
    {
    }

    public void onServerSwitch()
    {
    }
}
