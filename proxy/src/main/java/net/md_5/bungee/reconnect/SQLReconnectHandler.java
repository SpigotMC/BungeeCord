package net.md_5.bungee.reconnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SQLReconnectHandler extends AbstractReconnectManager
{

    private final Connection connection;

    public SQLReconnectHandler() throws ClassNotFoundException, SQLException
    {
        Class.forName( "org.sqlite.JDBC" );
        connection = DriverManager.getConnection( "jdbc:sqlite:bungee.sqlite" );

        try ( PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS players ("
                + "playerId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE COLLATE NOCASE,"
                + "seen INTEGER,"
                + "server TEXT"
                + ");" ) )
        {
            ps.executeUpdate();
        }
    }

    @Override
    protected synchronized ServerInfo getStoredServer(ProxiedPlayer player)
    {
        String server = null;
        try ( PreparedStatement ps = connection.prepareStatement( "SELECT server FROM players WHERE username = ?" ) )
        {
            ps.setString( 1, player.getName() );
            try ( ResultSet rs = ps.executeQuery() )
            {
                if ( rs.next() )
                {
                    server = rs.getString( 1 );
                } else
                {
                    try ( PreparedStatement playerUpdate = connection.prepareStatement( "INSERT INTO players( username ) VALUES( ? )" ) )
                    {
                        playerUpdate.setString( 1, player.getName() );
                        playerUpdate.executeUpdate();
                    }
                }
            }
        } catch ( SQLException ex )
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load location for player " + player.getName(), ex );
        }

        return ProxyServer.getInstance().getServerInfo( server );
    }

    @Override
    public synchronized void setServer(ProxiedPlayer player)
    {

        try ( PreparedStatement ps = connection.prepareStatement( "UPDATE players SET server = ?, seen = ? WHERE username = ?" ) )
        {
            ps.setString( 1, player.getServer().getInfo().getName() );
            ps.setInt( 2, (int) ( System.currentTimeMillis() / 1000L ) );
            ps.setString( 3, player.getName() );
            ps.executeUpdate();
        } catch ( SQLException ex )
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not save location for player " + player.getName() + " on server " + player.getServer().getInfo().getName(), ex );
        }
    }

    @Override
    public void save()
    {
    }

    @Override
    public synchronized void close()
    {
        try
        {
            connection.close();
        } catch ( SQLException ex )
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Error closing SQLite connection", ex );
        }
    }
}
