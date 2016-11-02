package net.md_5.bungee.api;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class AbstractReconnectHandler implements ReconnectHandler
{

    @Override
    public ServerInfo getServer(ProxiedPlayer player)
    {
        ServerInfo server = getForcedHost( player.getPendingConnection() );
        if ( server == null )
        {
            server = getStoredServer( player );
            if ( server == null )
            {
                server = ProxyServer.getInstance().getServerInfo( player.getPendingConnection().getListener().getServerPriority().get(0) );
            }

            Preconditions.checkState( server != null, "Default server not defined" );
        }

        return server;
    }

    /**
     * Returns the info for the forced host if its configured else it returns the default host.
     * @param con then PendingConnection.
     * @return ServerInfo for the forced host or the default...if not configured
     */
    public static ServerInfo getForcedHost(PendingConnection con)
    {
        if ( con.getVirtualHost() == null )
        {
            return null;
        }

        String forced = con.getListener().getForcedHosts().get( con.getVirtualHost().getHostString() );

        if ( forced == null && con.getListener().isForceDefault() )
        {
            forced = con.getListener().getServerPriority().get(0); //the default host
        }
        return ProxyServer.getInstance().getServerInfo( forced );
    }

    protected abstract ServerInfo getStoredServer(ProxiedPlayer player);
}
