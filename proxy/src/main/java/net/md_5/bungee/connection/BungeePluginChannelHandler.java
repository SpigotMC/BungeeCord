package net.md_5.bungee.connection;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;

import java.io.DataInput;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class BungeePluginChannelHandler {

    private final DownstreamBridge bridge;

    public void handle(DataInput in, PluginMessageEvent event) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        String subChannel = in.readUTF();

        // Use reflection to skip needing a huge set of switch cases or if statements
        Method handler = this.getClass().getDeclaredMethod("handle" + subChannel);
        if ( handler == null ) { return; }

        handler.invoke(this, in, out);

        // Check we haven't set out to null, and we have written data, if so reply back back along the BungeeCord channel
        if ( out == null || out.toByteArray().length == 0 ) { return; }
        bridge.getCon().getServer().sendData("BungeeCord", out.toByteArray());
    }

    private void handleConnect(DataInput in, ByteArrayDataOutput out) throws Exception {
        ServerInfo server = bridge.getBungee().getServerInfo(in.readUTF());
        if ( server == null ) { return; }
        bridge.getCon().connect(server);
    }

    private void handleConnectOther(DataInput in, ByteArrayDataOutput out) throws Exception {
        ProxiedPlayer player = bridge.getBungee().getPlayer( in.readUTF() );
        ServerInfo server = bridge.getBungee().getServerInfo( in.readUTF() );
        if (player == null || server == null) { return; }
        player.connect(server);
    }

    private void handleIP(DataInput in, ByteArrayDataOutput out) throws Exception {
        out.writeUTF( "IP" );
        out.writeUTF( bridge.getCon().getAddress().getHostString() );
        out.writeInt(bridge.getCon().getAddress().getPort());
    }

    private void handleKickPlayer(DataInput in, ByteArrayDataOutput out) throws Exception {
        ProxiedPlayer player = bridge.getBungee().getPlayer(in.readUTF());
        if ( player == null ) { return; }
        String kickReason = in.readUTF();
        player.disconnect( new TextComponent( kickReason ) );
    }

    private void handleForward(DataInput in, ByteArrayDataOutput out) throws Exception {
        // Read data from server
        String target = in.readUTF();
        String channel = in.readUTF();
        short len = in.readShort();
        byte[] data = new byte[ len ];
        in.readFully(data);

        ByteArrayDataOutput msgout = ByteStreams.newDataOutput();

        // Prepare new data to send
        msgout.writeUTF(channel);
        msgout.writeShort(data.length);
        msgout.write( data );
        byte[] payload = msgout.toByteArray();

        if (target.equals("ALL") || target.equals("ONLINE")) {
            for (ServerInfo server : bridge.getBungee().getServers().values()) {
                if (server != bridge.getServer().getInfo()) {
                    // last argument specifies if message should be queued for offline servers
                    server.sendData( "BungeeCord", payload, target.equals("ALL"));
                }
            }
        } else {
            ServerInfo server = bridge.getBungee().getServerInfo(target);
            if (server == null) { return; }
            server.sendData( "BungeeCord", payload);
        }

    }

    private void handleForwardToPlayer(DataInput in, ByteArrayDataOutput out) throws Exception {
        ByteArrayDataOutput msgout = ByteStreams.newDataOutput();

        ProxiedPlayer target = bridge.getBungee().getPlayer( in.readUTF() );
        if ( target == null ) { return; }
        // Read data from server
        String channel = in.readUTF();
        short len = in.readShort();
        byte[] data = new byte[ len ];
        in.readFully( data );

        // Prepare new data to send
        msgout.writeUTF( channel );
        msgout.writeShort( data.length );
        msgout.write( data );
        byte[] payload = msgout.toByteArray();

        target.getServer().sendData( "BungeeCord", payload );
    }

    private void handleGetServer(DataInput in, ByteArrayDataOutput out) throws Exception {
        out.writeUTF( "GetServer" );
        out.writeUTF( bridge.getServer().getInfo().getName() );
    }

    private void handleGetServers(DataInput in, ByteArrayDataOutput out) throws Exception {
        out.writeUTF( "GetServers" );
        out.writeUTF(Util.csv(bridge.getBungee().getServers().keySet()));
    }

    private void handleMessage(DataInput in, ByteArrayDataOutput out) throws Exception {
        ProxiedPlayer target = bridge.getBungee().getPlayer(in.readUTF());
        if ( target == null ) { return; }
        target.sendMessage( new TextComponent( in.readUTF() ) );
    }

    private void handlePlayerList(DataInput in, ByteArrayDataOutput out) throws Exception {
        String target = in.readUTF();
        out.writeUTF( "PlayerList" );
        out.writeUTF(target);
        if ( target.equals( "ALL" ) ) {
            out.writeUTF( Util.csv( bridge.getBungee().getPlayers() ) );
        } else {
            ServerInfo server = bridge.getBungee().getServerInfo(target);
            if (server == null) { return; }
            out.writeUTF( Util.csv( server.getPlayers() ) );
        }
    }

    private void handleServerIP(DataInput in, ByteArrayDataOutput out) throws Exception {
        ServerInfo info = bridge.getBungee().getServerInfo(in.readUTF());
        if (info == null) { return; }
        out.writeUTF( "ServerIP" );
        out.writeUTF( info.getName() );
        out.writeUTF( info.getAddress().getAddress().getHostAddress() );
        out.writeShort( info.getAddress().getPort() );
    }

    private void handleUUID(DataInput in, ByteArrayDataOutput out) throws Exception {
        out.writeUTF( "UUID" );
        out.writeUTF( bridge.getCon().getUUID() );
    }

    private void handleUUIDOther(DataInput in, ByteArrayDataOutput out) throws Exception {
        ProxiedPlayer player = bridge.getBungee().getPlayer(in.readUTF());
        if ( player == null ) { return; }
        out.writeUTF( "UUIDOther" );
        out.writeUTF( player.getName() );
        out.writeUTF( player.getUniqueId().toString() );
    }

}
