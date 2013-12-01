package net.md_5.bungee.connection;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.TabCompleteRequest;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.PluginMessage;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;

public class UpstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;

    public UpstreamBridge(ProxyServer bungee, UserConnection con)
    {
        this.bungee = bungee;
        this.con = con;

        BungeeCord.getInstance().addConnection( con );
        con.getTabList().onConnect();
        con.unsafe().sendPacket( BungeeCord.getInstance().registerChannels() );
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
        con.getTabList().onDisconnect();
        BungeeCord.getInstance().removeConnection( con );

        if ( con.getServer() != null )
        {
            con.getServer().setObsolete( true );
            con.getServer().disconnect( "Quitting" );
            con.setServer( null );
        }
        
        con.disconnect( "End of Stream" );
        
        if ( BungeeCord.isExitWhenEmpty() && BungeeCord.getInstance().getOnlineCount() == 0 )
            BungeeCord.getInstance().stop();
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception
    {
        // EntityMap.rewrite( packet.buf, con.getClientEntityId(), con.getServerEntityId() );
        if ( con.getServer() != null )
        {
            con.getServer().getCh().write( packet );
        }
    }

    @Override
    public void handle(KeepAlive alive) throws Exception
    {
        if ( alive.getRandomId() == con.getSentPingId() )
        {
            int newPing = (int) ( System.currentTimeMillis() - con.getSentPingTime() );
            con.getTabList().onPingChange( newPing );
            con.setPing( newPing );
        }
    }

    @Override
    public void handle(Chat chat) throws Exception
    {
        ChatEvent chatEvent = new ChatEvent( con, con.getServer(), chat.getMessage() );
        if ( !bungee.getPluginManager().callEvent( chatEvent ).isCancelled() )
        {
            chat.setMessage( chatEvent.getMessage() );
            if ( !chatEvent.isCommand() || !bungee.getPluginManager().dispatchCommand( con, chat.getMessage().substring( 1 ) ) )
            {
                con.getServer().unsafe().sendPacket( chat );
            }
        }
        throw new CancelSendSignal();
    }

    @Override
    public void handle(TabCompleteRequest tabComplete) throws Exception
    {
        if ( tabComplete.getCursor().startsWith( "/" ) )
        {
            List<String> results = new ArrayList<>();
            bungee.getPluginManager().dispatchCommand( con, tabComplete.getCursor().substring( 1 ), results );

            if ( !results.isEmpty() )
            {
                con.unsafe().sendPacket( new TabCompleteResponse( results.toArray( new String[ results.size() ] ) ) );
                throw new CancelSendSignal();
            }
        }
    }

    @Override
    public void handle(ClientSettings settings) throws Exception
    {
        con.setSettings( settings );
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        if ( pluginMessage.getTag().equals( "BungeeCord" ) )
        {
            throw new CancelSendSignal();
        }
        // Hack around Forge race conditions
        if ( pluginMessage.getTag().equals( "FML" ) && pluginMessage.getStream().readUnsignedByte() == 1 )
        {
            throw new CancelSendSignal();
        }

        PluginMessageEvent event = new PluginMessageEvent( con, con.getServer(), pluginMessage.getTag(), pluginMessage.getData().clone() );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            throw new CancelSendSignal();
        }

        // TODO: Unregister as well?
        if ( pluginMessage.getTag().equals( "REGISTER" ) )
        {
            con.getPendingConnection().getRegisterMessages().add( pluginMessage );
        }
    }
    
    @Override
    public void handle( Kick kick ) throws Exception {
        con.getServer().setObsolete( true );
        con.getServer().disconnect( "End of Stream" );
        con.setServer( null );
        con.disconnect( "End of stream" );
    }

    @Override
    public String toString()
    {
        return "[" + con.getName() + "] -> UpstreamBridge";
    }
}
