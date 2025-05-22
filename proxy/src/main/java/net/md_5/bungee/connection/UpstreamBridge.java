package net.md_5.bungee.connection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.ServerConnection.KeepAliveData;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.CustomClickEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.SettingsChangedEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.entitymap.EntityMap;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientChat;
import net.md_5.bungee.protocol.packet.ClientCommand;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.CookieResponse;
import net.md_5.bungee.protocol.packet.CustomClickAction;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.LoginAcknowledged;
import net.md_5.bungee.protocol.packet.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.StartConfiguration;
import net.md_5.bungee.protocol.packet.TabCompleteRequest;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;
import net.md_5.bungee.protocol.packet.UnsignedClientCommand;
import net.md_5.bungee.util.AllowedCharacters;

public class UpstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;

    public UpstreamBridge(ProxyServer bungee, UserConnection con)
    {
        this.bungee = bungee;
        this.con = con;

        con.getTabListHandler().onConnect();
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
        con.getTabListHandler().onDisconnect();
        BungeeCord.getInstance().removeConnection( con );

        if ( con.getServer() != null )
        {
            // Manually remove from everyone's tab list
            // since the packet from the server arrives
            // too late
            // TODO: This should only done with server_unique
            //       tab list (which is the only one supported
            //       currently)
            PlayerListItem oldPacket = new PlayerListItem();
            oldPacket.setAction( PlayerListItem.Action.REMOVE_PLAYER );
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid( con.getRewriteId() );
            oldPacket.setItems( new PlayerListItem.Item[]
            {
                item
            } );

            PlayerListItemRemove newPacket = new PlayerListItemRemove();
            newPacket.setUuids( new UUID[]
            {
                con.getRewriteId()
            } );

            for ( ProxiedPlayer player : con.getServer().getInfo().getPlayers() )
            {
                if ( player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_19_3 )
                {
                    // need to queue, because players in config state could receive it
                    player.unsafe().sendPacketQueued( newPacket );
                } else
                {
                    player.unsafe().sendPacket( oldPacket );
                }
            }
            con.getServer().disconnect( "Quitting" );
        }
    }

    @Override
    public void writabilityChanged(ChannelWrapper channel) throws Exception
    {
        if ( con.getServer() != null )
        {
            Channel server = con.getServer().getCh().getHandle();
            if ( channel.getHandle().isWritable() )
            {
                server.config().setAutoRead( true );
            } else
            {
                server.config().setAutoRead( false );
            }
        }
    }

    @Override
    public boolean shouldHandle(PacketWrapper packet) throws Exception
    {
        return con.getServer() != null || packet.packet instanceof PluginMessage || packet.packet instanceof CookieResponse || packet.packet instanceof LoginPayloadResponse;
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception
    {
        ServerConnection server = con.getServer();
        if ( server != null && server.isConnected() )
        {
            Protocol serverEncode = server.getCh().getEncodeProtocol();
            // #3527: May still have old packets from client in game state when switching server to configuration state - discard those
            if ( packet.protocol != serverEncode )
            {
                return;
            }

            EntityMap rewrite = con.getEntityRewrite();
            if ( rewrite != null && serverEncode == Protocol.GAME )
            {
                rewrite.rewriteServerbound( packet.buf, con.getClientEntityId(), con.getServerEntityId(), con.getPendingConnection().getVersion() );
            }
            server.getCh().write( packet );
        }
    }

    @Override
    public void handle(KeepAlive alive) throws Exception
    {
        KeepAliveData keepAliveData = con.getServer().getKeepAlives().peek();

        if ( keepAliveData != null && alive.getRandomId() == keepAliveData.getId() )
        {
            Preconditions.checkState( keepAliveData == con.getServer().getKeepAlives().poll(), "keepalive queue mismatch" );
            int newPing = (int) ( System.currentTimeMillis() - keepAliveData.getTime() );
            con.getTabListHandler().onPingChange( newPing );
            con.setPing( newPing );
        } else
        {
            throw CancelSendSignal.INSTANCE;
        }
    }

    @Override
    public void handle(Chat chat) throws Exception
    {
        String message = handleChat( chat.getMessage() );
        if ( message != null )
        {
            chat.setMessage( message );
            con.getServer().unsafe().sendPacket( chat );
        }

        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public void handle(ClientChat chat) throws Exception
    {
        handleChat( chat.getMessage() );
    }

    @Override
    public void handle(ClientCommand command) throws Exception
    {
        handleChat( "/" + command.getCommand() );
    }

    @Override
    public void handle(UnsignedClientCommand command) throws Exception
    {
        handleChat( "/" + command.getCommand() );
    }

    private String handleChat(String message)
    {
        for ( int index = 0, length = message.length(); index < length; index++ )
        {
            char c = message.charAt( index );
            if ( !AllowedCharacters.isChatAllowedCharacter( c ) )
            {
                con.disconnect( bungee.getTranslation( "illegal_chat_characters", Util.unicode( c ) ) );
                throw CancelSendSignal.INSTANCE;
            }
        }

        ChatEvent chatEvent = new ChatEvent( con, con.getServer(), message );
        if ( !bungee.getPluginManager().callEvent( chatEvent ).isCancelled() )
        {
            message = chatEvent.getMessage();
            if ( !chatEvent.isCommand() || !bungee.getPluginManager().dispatchCommand( con, message.substring( 1 ) ) )
            {
                return message;
            }
        }
        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public void handle(TabCompleteRequest tabComplete) throws Exception
    {
        List<String> suggestions = new ArrayList<>();
        boolean isRegisteredCommand = false;
        boolean isCommand = tabComplete.getCursor().startsWith( "/" );

        if ( isCommand )
        {
            isRegisteredCommand = bungee.getPluginManager().dispatchCommand( con, tabComplete.getCursor().substring( 1 ), suggestions );
        }

        TabCompleteEvent tabCompleteEvent = new TabCompleteEvent( con, con.getServer(), tabComplete.getCursor(), suggestions );
        bungee.getPluginManager().callEvent( tabCompleteEvent );

        if ( tabCompleteEvent.isCancelled() )
        {
            throw CancelSendSignal.INSTANCE;
        }

        List<String> results = tabCompleteEvent.getSuggestions();
        if ( !results.isEmpty() )
        {
            // Unclear how to handle 1.13 commands at this point. Because we don't inject into the command packets we are unlikely to get this far unless
            // Bungee plugins are adding results for commands they don't own anyway
            if ( con.getPendingConnection().getVersion() < ProtocolConstants.MINECRAFT_1_13 )
            {
                con.unsafe().sendPacket( new TabCompleteResponse( results ) );
            } else
            {
                int start = tabComplete.getCursor().lastIndexOf( ' ' ) + 1;
                int end = tabComplete.getCursor().length();
                StringRange range = StringRange.between( start, end );

                List<Suggestion> brigadier = new LinkedList<>();
                for ( String s : results )
                {
                    brigadier.add( new Suggestion( range, s ) );
                }

                con.unsafe().sendPacket( new TabCompleteResponse( tabComplete.getTransactionId(), new Suggestions( range, brigadier ) ) );
            }
            throw CancelSendSignal.INSTANCE;
        }

        // Don't forward tab completions if the command is a registered bungee command
        if ( isRegisteredCommand )
        {
            throw CancelSendSignal.INSTANCE;
        }

        if ( isCommand && con.getPendingConnection().getVersion() < ProtocolConstants.MINECRAFT_1_13 )
        {
            int lastSpace = tabComplete.getCursor().lastIndexOf( ' ' );
            if ( lastSpace == -1 )
            {
                con.setLastCommandTabbed( tabComplete.getCursor().substring( 1 ) );
            }
        }
    }

    @Override
    public void handle(ClientSettings settings) throws Exception
    {
        con.setSettings( settings );

        SettingsChangedEvent settingsEvent = new SettingsChangedEvent( con );
        bungee.getPluginManager().callEvent( settingsEvent );
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        if ( pluginMessage.getTag().equals( PluginMessage.BUNGEE_CHANNEL_LEGACY ) || pluginMessage.getTag().equals( PluginMessage.BUNGEE_CHANNEL_MODERN ) )
        {
            throw CancelSendSignal.INSTANCE;
        }

        if ( BungeeCord.getInstance().config.isForgeSupport() )
        {
            // Hack around Forge race conditions
            if ( pluginMessage.getTag().equals( "FML" ) && pluginMessage.getStream().readUnsignedByte() == 1 )
            {
                throw CancelSendSignal.INSTANCE;
            }

            // We handle forge handshake messages if forge support is enabled.
            if ( pluginMessage.getTag().equals( ForgeConstants.FML_HANDSHAKE_TAG ) )
            {
                // Let our forge client handler deal with this packet.
                con.getForgeClientHandler().handle( pluginMessage );
                throw CancelSendSignal.INSTANCE;
            }

            if ( con.getServer() != null && !con.getServer().isForgeServer() && pluginMessage.getData().length > Short.MAX_VALUE )
            {
                // Drop the packet if the server is not a Forge server and the message was > 32kiB (as suggested by @jk-5)
                // Do this AFTER the mod list, so we get that even if the intial server isn't modded.
                throw CancelSendSignal.INSTANCE;
            }
        }

        PluginMessageEvent event = new PluginMessageEvent( con, con.getServer(), pluginMessage.getTag(), pluginMessage.getData().clone() );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            throw CancelSendSignal.INSTANCE;
        }

        con.getPendingConnection().relayMessage( pluginMessage );
    }

    @Override
    public void handle(LoginAcknowledged loginAcknowledged) throws Exception
    {
        configureServer();
    }

    @Override
    public void handle(StartConfiguration startConfiguration) throws Exception
    {
        configureServer();
    }

    private void configureServer()
    {
        ChannelWrapper ch = con.getServer().getCh();
        if ( ch.getDecodeProtocol() == Protocol.LOGIN )
        {
            ch.setDecodeProtocol( Protocol.CONFIGURATION );
            ch.write( new LoginAcknowledged() );
            ch.setEncodeProtocol( Protocol.CONFIGURATION );

            con.getServer().sendQueuedPackets();

            throw CancelSendSignal.INSTANCE;
        }
    }

    @Override
    public void handle(CookieResponse cookieResponse) throws Exception
    {
        con.getPendingConnection().handle( cookieResponse );
    }

    @Override
    public void handle(LoginPayloadResponse loginPayloadResponse) throws Exception
    {
        con.getPendingConnection().handle( loginPayloadResponse );
    }

    @Override
    public void handle(CustomClickAction customClickAction) throws Exception
    {
        Map<String, String> data = null;
        if ( customClickAction.getData() != null )
        {
            ImmutableMap.Builder<String, String> parsed = ImmutableMap.builder();

            String[] lines = customClickAction.getData().split( "\n" );
            for ( String line : lines )
            {
                String[] split = line.split( "\t", 2 );
                if ( split.length > 1 )
                {
                    parsed.put( split[0], split[1] );
                }
            }
            data = parsed.buildOrThrow();

            if ( data.isEmpty() )
            {
                data = null;
            }
        }

        CustomClickEvent event = new CustomClickEvent( con, customClickAction.getId(), customClickAction.getData(), data );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            throw CancelSendSignal.INSTANCE;
        }
    }

    @Override
    public String toString()
    {
        return "[" + con.getName() + "] -> UpstreamBridge";
    }
}
