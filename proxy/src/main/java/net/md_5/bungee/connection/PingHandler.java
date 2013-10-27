package net.md_5.bungee.connection;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;

@RequiredArgsConstructor
public class PingHandler extends PacketHandler
{

    private final ServerInfo target;
    private final Callback<ServerPing> callback;
    private ChannelWrapper channel;

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.channel = channel;

        channel.write( new Handshake( Protocol.PROTOCOL_VERSION, target.getAddress().getHostString(), target.getAddress().getPort(), 1 ) );
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
