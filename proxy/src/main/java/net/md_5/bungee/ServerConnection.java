package net.md_5.bungee;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Kick;

@RequiredArgsConstructor
public class ServerConnection implements Server
{

    @Getter
    private final ChannelWrapper ch;
    @Getter
    private final BungeeServerInfo info;
    @Getter
    @Setter
    private boolean isObsolete;
    @Getter
    private final boolean forgeServer = false;

    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }
    };

    @Override
    public void sendData(String channel, byte[] data)
    {
        unsafe().sendPacket( new PluginMessage( channel, data, forgeServer ) );
    }

    @Override
    public void disconnect(String reason)
    {
        disconnect( TextComponent.fromLegacyText( reason ) );
    }

    @Override
    public synchronized void disconnect(BaseComponent... reason)
    {
        if ( !ch.isClosed() )
        {
            ch.getHandle().eventLoop().schedule( new Runnable()
            {
                @Override
                public void run()
                {
                    ch.getHandle().close();
                }
            }, 100, TimeUnit.MILLISECONDS );
        }

    }

    @Override
    public void disconnect(BaseComponent reason)
    {
        disconnect( new BaseComponent[]
        {
            reason
        } );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return getInfo().getAddress();
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }
}
