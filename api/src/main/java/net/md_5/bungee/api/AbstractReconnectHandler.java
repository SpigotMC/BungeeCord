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
                server = ProxyServer.getInstance().getServerInfo( player.getPendingConnection().getListener().getDefaultServer() );
            }

            Preconditions.checkState( server != null, "Default server not defined" );
        }

        return server;
    }

    public static ServerInfo getForcedHost(PendingConnection con)
    {
        String forced = ( con.getVirtualHost() == null ) ? null : con.getListener().getForcedHosts().get( con.getVirtualHost().getHostString() );

        if ( forced == null && con.getListener().isForceDefault() )
        {
            forced = con.getListener().getDefaultServer();
        }
        return ( forced == null ) ? null : ProxyServer.getInstance().getServerInfo( forced );
    }

    protected abstract ServerInfo getStoredServer(ProxiedPlayer player);
}
