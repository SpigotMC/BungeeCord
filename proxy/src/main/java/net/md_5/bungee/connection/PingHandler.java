package net.md_5.bungee.connection;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.packet.PacketFFKick;

@RequiredArgsConstructor
public class PingHandler extends PacketHandler
{

    private final ServerInfo target;
    private final Callback<ServerPing> callback;
    private static final byte[] pingBuf = new byte[]
    {
        (byte) 0xFE, (byte) 0x01
    };

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        channel.write( pingBuf );
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        callback.done( null, t );
    }

    @Override
    public void handle(PacketFFKick kick) throws Exception
    {
        String[] split = kick.getMessage().split( "\00" );
        ServerPing ping = new ServerPing( Byte.parseByte( split[1] ), split[2], split[3], Integer.parseInt( split[4] ), Integer.parseInt( split[5] ) );
        callback.done( ping, null );
    }

    @Override
    public String toString()
    {
        return "[Ping Handler] -> " + target.getName();
    }
}
