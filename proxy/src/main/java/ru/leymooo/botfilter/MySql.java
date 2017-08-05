package ru.leymooo.botfilter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.md_5.bungee.config.Configuration;

/**
 *
 * @author Jampire
 */
public class MySql
{

    private Connection connection;
    private ExecutorService executor;
    private String host;
    private String database;
    private String user;
    private String password;
    private String port;
    private static Thread syncThread;

    public MySql(Configuration section)
    {
        this.host = section.getString( "host" );
        this.user = section.getString( "username" );
        this.password = section.getString( "password" );
        this.database = section.getString( "database" );
        this.port = section.getString( "port" );
        this.executor = Executors.newSingleThreadExecutor();
        try
        {
            this.connect();
            try ( Statement st = this.getConnection().createStatement() )
            {
                st.executeUpdate( "CREATE TABLE IF NOT EXISTS `Players` (`player` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE, `ip` VARCHAR(16) NOT NULL);" );
            }
            try ( Statement statement = this.getConnection().createStatement(); ResultSet rs = statement.executeQuery( "SELECT * FROM `Players`;" ) )
            {
                while ( rs.next() )
                {
                    Config.getConfig().addUserToMap( rs.getString( "player" ).toLowerCase(), rs.getString( "ip" ) );
                }
                System.out.println( "[BotFilter] Белый список игроков успешно загружен." );
            }
        } catch ( SQLException ex )
        {
            ex.printStackTrace();
        }
        if ( section.getBoolean( "multibungee.enabled" ) )
        {
            syncThread( section.getInt( "multibungee.sync-time", 10 ) );
        }
    }

    private Connection getConnection() throws SQLException
    {
        if ( this.connection == null || this.connection.isClosed() )
        {
            this.connect();
        }
        return this.connection;
    }

    public void addAddress(final String name, final String ip)
    {
        this.executor.execute( () ->
        {
            {
                try ( Statement statment = getConnection().createStatement() )
                {
                    statment.executeUpdate( "INSERT INTO `Players` (`player`,`ip`) VALUES ('" + name + "','" + ip + "') ON DUPLICATE KEY UPDATE `ip`='" + ip + "';" );
                } catch ( SQLException e )
                {
                    e.printStackTrace();
                }
            }
        } );
    }

    private void connect() throws SQLException
    {
        try
        {
            if ( this.connection != null )
            {
                this.connection.close();
            }
        } catch ( SQLException ignored )
        {
        }

        System.out.println( "[SQL] Connect to " + this.host );
        long start = System.currentTimeMillis();

        this.connection = DriverManager.getConnection( "JDBC:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password );
        System.out.println( "[SQL] Connected [" + ( System.currentTimeMillis() - start ) + " ms]" );
    }

    private void syncThread(int syncTime)
    {
        stopSyncThread();
        ( syncThread = new Thread( () ->
        {
            while ( !Thread.interrupted() )
            {
                try
                {
                    Thread.sleep( syncTime * 1000 );
                    try ( Statement statement = this.getConnection().createStatement(); ResultSet rs = statement.executeQuery( "SELECT * FROM `Players`;" ) )
                    {
                        while ( rs.next() )
                        {
                            Config.getConfig().addUserToMap( rs.getString( "player" ).toLowerCase(), rs.getString( "ip" ) );
                        }
                    }
                } catch ( InterruptedException ex )
                {
                    return;
                } catch ( SQLException ex )
                {
                    ex.printStackTrace();
                }
            }
        }, "DataBase sync" ) ).start();
    }

    public static void stopSyncThread()
    {
        if ( syncThread != null && syncThread.isAlive() )
        {
            syncThread.interrupt();
        }
    }
}
