package net.md_5.bungee.tab;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class Empty extends TabList
{

    public Empty(ProxiedPlayer player)
    {
        super( player );
    }

    @Override
    public void onUpdate(PlayerListItem playerListItem)
    {

    }

    @Override
    public void onPingChange(int ping)
    {

    }

    @Override
    public void onServerChange()
    {

    }

    @Override
    public void onConnect()
    {

    }

    @Override
    public void onDisconnect()
    {

    }
}
