package net.md_5.bungee.connection;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.packet.Packet0KeepAlive;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.PacketC9PlayerListItem;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketHandler;

@RequiredArgsConstructor
public class DownstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;

    @Override
    public void handle(Packet0KeepAlive alive) throws Exception
    {
        con.trackingPingId = alive.id;
    }

    @Override
    public void handle(Packet3Chat chat) throws Exception
    {
        ChatEvent chatEvent = new ChatEvent( con.getServer(), con, chat.message );
        bungee.getPluginManager().callEvent( chatEvent );

        if ( chatEvent.isCancelled() )
        {
            throw new CancelSendSignal();
        }
    }

    @Override
    public void handle(PacketC9PlayerListItem playerList) throws Exception
    {

        if ( !bungee.getTabListHandler().onListUpdate( con, playerList.username, playerList.online, playerList.ping ) )
        {
            throw new CancelSendSignal();
        }
    }

    @Override
    public void handle(PacketFAPluginMessage pluginMessage) throws Exception
    {
        ByteArrayDataInput in = ByteStreams.newDataInput( pluginMessage.data );
        PluginMessageEvent event = new PluginMessageEvent( con.getServer(), con, pluginMessage.tag, pluginMessage.data.clone() );

        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            throw new CancelSendSignal();
        }

        if ( pluginMessage.tag.equals( "BungeeCord" ) )
        {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            String subChannel = in.readUTF();

            if ( subChannel.equals( "Forward" ) )
            {
                // Read data from server
                String target = in.readUTF();
                String channel = in.readUTF();
                short len = in.readShort();
                byte[] data = new byte[ len ];
                in.readFully( data );

                // Prepare new data to send
                out.writeUTF( channel );
                out.writeShort( data.length );
                out.write( data );
                byte[] payload = out.toByteArray();

                // Null out stream, important as we don't want to send to ourselves
                out = null;

                if ( target.equals( "ALL" ) )
                {
                    for ( ServerInfo server : bungee.getServers().values() )
                    {
                        if ( server != con.getServer().getInfo() )
                        {
                            server.sendData( "BungeeCord", payload );
                        }
                    }
                } else
                {
                    ServerInfo server = bungee.getServerInfo( target );
                    if ( server != null )
                    {
                        server.sendData( "BungeeCord", payload );
                    }
                }
            }
            if ( subChannel.equals( "Connect" ) )
            {
                ServerInfo server = bungee.getServerInfo( in.readUTF() );
                if ( server != null )
                {
                    connect( server, true );
                    break outer;
                }
            }
            if ( subChannel.equals( "IP" ) )
            {
                out.writeUTF( "IP" );
                out.writeUTF( con.getAddress().getHostString() );
                out.writeInt( con.getAddress().getPort() );
            }
            if ( subChannel.equals( "PlayerCount" ) )
            {
                ServerInfo server = bungee.getServerInfo( in.readUTF() );
                if ( server != null )
                {
                    out.writeUTF( "PlayerCount" );
                    out.writeUTF( server.getName() );
                    out.writeInt( server.getPlayers().size() );
                }
            }
            if ( subChannel.equals( "PlayerList" ) )
            {
                ServerInfo server = bungee.getServerInfo( in.readUTF() );
                if ( server != null )
                {
                    out.writeUTF( "PlayerList" );
                    out.writeUTF( server.getName() );

                    StringBuilder sb = new StringBuilder();
                    for ( ProxiedPlayer p : server.getPlayers() )
                    {
                        sb.append( p.getName() );
                        sb.append( "," );
                    }
                    out.writeUTF( sb.substring( 0, sb.length() - 1 ) );
                }
            }
            if ( subChannel.equals( "GetServers" ) )
            {
                out.writeUTF( "GetServers" );

                StringBuilder sb = new StringBuilder();
                for ( String server : bungee.getServers().keySet() )
                {
                    sb.append( server );
                    sb.append( "," );
                }
                out.writeUTF( sb.substring( 0, sb.length() - 1 ) );
            }
            if ( subChannel.equals( "Message" ) )
            {
                ProxiedPlayer target = bungee.getPlayer( in.readUTF() );
                if ( target != null )
                {
                    target.sendMessage( in.readUTF() );
                }
            }

            // Check we haven't set out to null, and we have written data, if so reply back back along the BungeeCord channel
            if ( out != null )
            {
                byte[] b = out.toByteArray();
                if ( b.length != 0 )
                {
                    con.getServer().sendData( "BungeeCord", b );
                }
            }
        }
    }

    @Override
    public void handle(PacketFFKick kick) throws Exception
    {
        con.disconnect( "[Kicked] " + kick.message );
        throw new CancelSendSignal();
    }
}
