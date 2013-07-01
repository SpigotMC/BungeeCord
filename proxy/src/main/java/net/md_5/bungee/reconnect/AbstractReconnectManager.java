package net.md_5.bungee.reconnect;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class AbstractReconnectManager implements ReconnectHandler
{

    @Override
    public ServerInfo getServer(ProxiedPlayer player)
    {
        ListenerInfo listener = player.getPendingConnection().getListener();
        String name;
        ServerInfo forced = getHost( player.getPendingConnection() );

        String server = ( forced == null ) ? getStoredServer( player ) : forced.getName();
        name = ( server != null ) ? server : listener.getDefaultServer();
        ServerInfo info = ProxyServer.getInstance().getServerInfo( name );
        if ( info == null )
        {
            info = ProxyServer.getInstance().getServerInfo( listener.getDefaultServer() );
        }
        Preconditions.checkState( info != null, "Default server not defined" );
        return info;
    }

    public static ServerInfo getHost(PendingConnection con)
    {
        String forced = con.getListener().getForcedHosts().get( con.getVirtualHost().getHostString() );

        if ( forced == null && con.getListener().isForceDefault() )
        {
            forced = con.getListener().getDefaultServer();
        }
        return ( forced != null ) ? ProxyServer.getInstance().getServerInfo( forced ) : null;
    }

    protected abstract String getStoredServer(ProxiedPlayer player);
}
