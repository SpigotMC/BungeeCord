package net.md_5.bungee.api.tab;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@NoArgsConstructor
public abstract class TabListAdapter implements TabListHandler
{

    @Getter
    private ProxiedPlayer player;

    @Override
    public void init(ProxiedPlayer player)
    {
        this.player = player;
    }

    @Override
    public void onConnect()
    {
    }

    @Override
    public void onDisconnect()
    {
    }

    @Override
    public void onServerChange()
    {
    }

    @Override
    public void onPingChange(int ping)
    {
    }
}
