package net.md_5.bungee.connection;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.packet.Packet0KeepAlive;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketHandler;

@RequiredArgsConstructor
public class UpstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;

    @Override
    public void handle(Packet0KeepAlive alive) throws Exception
    {
        if ( alive.id == con.trackingPingId )
        {
            int newPing = (int) ( System.currentTimeMillis() - con.pingTime );
            bungee.getTabListHandler().onPingChange( con, newPing );
            con.setPing( newPing );
        }
    }

    @Override
    public void handle(Packet3Chat chat) throws Exception
    {
        if ( chat.message.charAt( 0 ) == '/' )
        {
            if ( bungee.getPluginManager().dispatchCommand( con, chat.message.substring( 1 ) ) )
            {
                throw new CancelSendSignal();
            }
        } else
        {
            ChatEvent chatEvent = new ChatEvent( con, con.getServer(), chat.message );
            if ( bungee.getPluginManager().callEvent( chatEvent ).isCancelled() )
            {
                throw new CancelSendSignal();
            }
        }
    }

    @Override
    public void handle(PacketFAPluginMessage pluginMessage) throws Exception
    {
        if ( pluginMessage.tag.equals( "BungeeCord" ) )
        {
            throw new CancelSendSignal();
        }

        PluginMessageEvent event = new PluginMessageEvent( con, con.getServer(), pluginMessage.tag, pluginMessage.data.clone() );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            throw new CancelSendSignal();
        }
    }
}
