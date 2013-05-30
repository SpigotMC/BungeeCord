package net.md_5.bungee.connection;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EntityMap;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.TexturePackInfo;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.packet.Packet0KeepAlive;
import net.md_5.bungee.protocol.packet.Packet3Chat;
import net.md_5.bungee.protocol.packet.PacketCCSettings;
import net.md_5.bungee.protocol.packet.PacketFAPluginMessage;

public class UpstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;

    public UpstreamBridge(ProxyServer bungee, UserConnection con)
    {
        this.bungee = bungee;
        this.con = con;

        BungeeCord.getInstance().addConnection( con );
        bungee.getTabListHandler().onConnect( con );
        con.sendPacket( BungeeCord.getInstance().registerChannels() );

        TexturePackInfo texture = con.getPendingConnection().getListener().getTexturePack();
        if ( texture != null )
        {
            con.setTexturePack( texture );
        }
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        con.disconnect( Util.exception( t ) );
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        // We lost connection to the client
        PlayerDisconnectEvent event = new PlayerDisconnectEvent( con );
        bungee.getPluginManager().callEvent( event );
        bungee.getTabListHandler().onDisconnect( con );
        BungeeCord.getInstance().removeConnection( con );

        if ( con.getServer() != null )
        {
            con.getServer().disconnect( "Quitting" );
        }
    }

    @Override
    public void handle(byte[] buf) throws Exception
    {
        EntityMap.rewrite( buf, con.getClientEntityId(), con.getServerEntityId() );
        if ( con.getServer() != null )
        {
            con.getServer().getCh().write( buf );
        }
    }

    @Override
    public void handle(Packet0KeepAlive alive) throws Exception
    {
        if ( alive.getRandomId() == con.getSentPingId() )
        {
            int newPing = (int) ( System.currentTimeMillis() - con.getSentPingTime() );
            bungee.getTabListHandler().onPingChange( con, newPing );
            con.setPing( newPing );
        }
    }

    @Override
    public void handle(Packet3Chat chat) throws Exception
    {
        ChatEvent chatEvent = new ChatEvent( con, con.getServer(), chat.getMessage() );
        if ( bungee.getPluginManager().callEvent( chatEvent ).isCancelled() )
        {
            throw new CancelSendSignal();
        }
        if ( chatEvent.isCommand() )
        {
            if ( bungee.getPluginManager().dispatchCommand( con, chat.getMessage().substring( 1 ) ) )
            {
                throw new CancelSendSignal();
            }
        }
    }

    @Override
    public void handle(PacketCCSettings settings) throws Exception
    {
        con.setSettings( settings );
    }

    @Override
    public void handle(PacketFAPluginMessage pluginMessage) throws Exception
    {
        if ( pluginMessage.getTag().equals( "BungeeCord" ) )
        {
            throw new CancelSendSignal();
        }

        PluginMessageEvent event = new PluginMessageEvent( con, con.getServer(), pluginMessage.getTag(), pluginMessage.getData().clone() );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            throw new CancelSendSignal();
        }
    }

    @Override
    public String toString()
    {
        return "[" + con.getName() + "] -> UpstreamBridge";
    }
}
