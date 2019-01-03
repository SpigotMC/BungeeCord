package net.md_5.bungee.api;

import java.net.InetSocketAddress;
import java.util.Collection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import org.junit.Assert;
import org.junit.Test;

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
    public void testDefaultConnectTimeout()
    {
        ServerConnectRequest request = ServerConnectRequest.builder().target( DUMMY_INFO ).reason( ServerConnectEvent.Reason.JOIN_PROXY ).build();
        Assert.assertEquals( 5000, request.getConnectTimeout() );
    }

    @Test(expected = NullPointerException.class)
    public void testNullTarget()
    {
        ServerConnectRequest.builder().target( null ).reason( ServerConnectEvent.Reason.JOIN_PROXY ).build();
    }

    @Test(expected = NullPointerException.class)
    public void testNullReason()
    {
        ServerConnectRequest.builder().target( DUMMY_INFO ).reason( null ).build();
    }
}
