package net.md_5.bungee.connection;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
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
import net.md_5.bungee.packet.Packet0KeepAlive;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.PacketCCSettings;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketHandler;

public class UpstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;

    public UpstreamBridge(ProxyServer bungee, UserConnection con)
    {
        this.bungee = bungee;
        this.con = con;

        BungeeCord.getInstance().connections.put( con.getName(), con );
        bungee.getTabListHandler().onConnect( con );
        con.sendPacket( BungeeCord.getInstance().registerChannels() );

        TexturePackInfo texture = con.getPendingConnection().getListener().getTexturePack();
        if ( texture != null )
        {
            con.sendPacket( new PacketFAPluginMessage( "MC|TPack", ( texture.getUrl() + "\00" + texture.getSize() ).getBytes() ) );
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
        BungeeCord.getInstance().connections.remove( con.getName() ); //TODO: Better way, why do we need to raw access?

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
        if ( alive.id == con.getSentPingId() )
        {
            int newPing = (int) ( System.currentTimeMillis() - con.getSentPingTime() );
            bungee.getTabListHandler().onPingChange( con, newPing );
            con.setPing( newPing );
        }
    }

    @Override
    public void handle(Packet3Chat chat) throws Exception
    {
        ChatEvent chatEvent = new ChatEvent( con, con.getServer(), chat.message );
        if ( bungee.getPluginManager().callEvent( chatEvent ).isCancelled() )
        {
            throw new CancelSendSignal();
        }
        if ( chatEvent.isCommand() )
        {
            if ( bungee.getPluginManager().dispatchCommand( con, chat.message.substring( 1 ) ) )
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
    
    @Override
    public void handle( PacketFFKick kick ) throws Exception {
        con.disconnect( "End of stream" );
    }

    @Override
    public String toString()
    {
        return "[" + con.getName() + "] -> UpstreamBridge";
    }
}
