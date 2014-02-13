package net.md_5.bungee.connection;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;

@RequiredArgsConstructor
public class PingHandler extends PacketHandler
{

    private final ServerInfo target;
    private final Callback<ServerPing> callback;
    private final PendingConnection pendingConnection;
    private ChannelWrapper channel;

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.channel = channel;
        MinecraftEncoder encoder = new MinecraftEncoder( Protocol.HANDSHAKE, false, ProxyServer.getInstance().getProtocolVersion() );

        channel.getHandle().pipeline().addAfter( PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder( Protocol.STATUS, false, ProxyServer.getInstance().getProtocolVersion() ) );
        channel.getHandle().pipeline().addAfter( PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, encoder );

        channel.write( new Handshake( ( pendingConnection != null ) ? pendingConnection.getVersion() : ProxyServer.getInstance().getProtocolVersion(), ( pendingConnection != null && BungeeCord.getInstance().config.isIpFoward() ) ? target.getAddress().getHostString() + "\00" + pendingConnection.getIp() + "\00" + pendingConnection.getUUID() : target.getAddress().getHostString(), target.getAddress().getPort(), 1 ) );

        encoder.setProtocol( Protocol.STATUS );
        channel.write( new StatusRequest() );
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        callback.done( null, t );
    }

    @Override
    public void handle(StatusResponse statusResponse) throws Exception
    {
        callback.done( BungeeCord.getInstance().gson.fromJson( statusResponse.getResponse(), ServerPing.class ), null );
        channel.close();
    }

    @Override
    public String toString()
    {
        return "[Ping Handler] -> " + target.getName();
    }
}
