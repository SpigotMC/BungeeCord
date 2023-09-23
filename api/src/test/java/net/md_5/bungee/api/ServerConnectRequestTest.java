package net.md_5.bungee.api;

import static org.junit.jupiter.api.Assertions.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import org.junit.jupiter.api.Test;

public class ServerConnectRequestTest
{

    private static final ServerInfo DUMMY_INFO = new ServerInfo()
    {
        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public SocketAddress getSocketAddress()
        {
            return null;
        }

        @Override
        public InetSocketAddress getAddress()
        {
            return null;
        }

        @Override
        public Collection<ProxiedPlayer> getPlayers()
        {
            return null;
        }

        @Override
        public String getMotd()
        {
            return null;
        }

        @Override
        public boolean isRestricted()
        {
            return false;
        }

        @Override
        public String getPermission()
        {
            return null;
        }

        @Override
        public boolean canAccess(CommandSender sender)
        {
            return true;
        }

        @Override
        public void sendData(String channel, byte[] data)
        {
        }

        @Override
        public boolean sendData(String channel, byte[] data, boolean queue)
        {
            return false;
        }

        @Override
        public void ping(Callback<ServerPing> callback)
        {
        }
    };

    @Test
    public void testNullTarget()
    {
        assertThrows( NullPointerException.class, () -> ServerConnectRequest.builder().target( null ).reason( ServerConnectEvent.Reason.JOIN_PROXY ).build() );
    }

    @Test
    public void testNullReason()
    {
        assertThrows( NullPointerException.class, () -> ServerConnectRequest.builder().target( DUMMY_INFO ).reason( null ).build() );
    }
}
