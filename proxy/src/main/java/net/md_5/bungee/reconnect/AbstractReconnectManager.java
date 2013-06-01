package net.md_5.bungee.reconnect;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class AbstractReconnectManager implements ReconnectHandler
{

    @Override
    public ServerInfo getServer(ProxiedPlayer player)
    {
        ListenerInfo listener = player.getPendingConnection().getListener();
        String name;
        String forced = listener.getForcedHosts().get( player.getPendingConnection().getVirtualHost().getHostName().toLowerCase() );
        if ( forced == null && listener.isForceDefault() )
        {
            forced = listener.getDefaultServer();
        }

        String server = ( forced == null ) ? getStoredServer( player ) : forced;
        name = ( server != null ) ? server : listener.getDefaultServer();
        ServerInfo info = ProxyServer.getInstance().getServerInfo( name );
        if ( info == null )
        {
            info = ProxyServer.getInstance().getServerInfo( listener.getDefaultServer() );
        }
        Preconditions.checkState( info != null, "Default server not defined" );
        return info;
    }

    protected abstract String getStoredServer(ProxiedPlayer player);
}
