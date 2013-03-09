package net.md_5.bungee.connection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketHandler;

@RequiredArgsConstructor
public class PingHandler extends PacketHandler
{

    private final ServerInfo target;
    private final Callback<ServerPing> callback;
    private static final ByteBuf pingBuf = Unpooled.wrappedBuffer( new byte[]
    {
        (byte) 0xFE, (byte) 0x01
    } );

    @Override
    public void connected(Channel channel) throws Exception
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
        String[] split = kick.message.split( "\00" );
        ServerPing ping = new ServerPing( Byte.parseByte( split[1] ), split[2], split[3], Integer.parseInt( split[4] ), Integer.parseInt( split[5] ) );
        callback.done( ping, null );
    }

    @Override
    public String toString()
    {
        return "[Ping Handler] -> " + target.getName();
    }
}
