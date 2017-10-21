package ru.leymooo.botfilter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 *
 * @author Jampire
 */
public class Sql
{

    private Connection connection;
    private ExecutorService executor;
    private boolean mysql;
    private int purgeTime;
    private String host;
    private String database;
    private String user;
    private String password;
    private String port;
    private Thread syncThread;

    public Sql(Configuration section)
    {
        this.mysql = section.getBoolean( "mysql" );
        this.purgeTime = section.getInt( "purge-time" );
        this.executor = Executors.newSingleThreadExecutor();
        if ( mysql )
        {
            section = section.getSection( "mysql" );
            this.host = section.getString( "host" );
            this.user = section.getString( "username" );
            this.password = section.getString( "password" );
            this.database = section.getString( "database" );
            this.port = section.getString( "port" );
            if ( section.getBoolean( "multibungee.enabled" ) )
            {
                syncThread( section.getInt( "multibungee.sync-time", 10 ) );
            }
        }
        try
        {
            this.connect();
            try ( Statement st = this.getConnection().createStatement() )
            {
                st.executeUpdate( "CREATE TABLE IF NOT EXISTS `Players` (`player` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE, `ip` VARCHAR(16) NOT NULL, `check-time` TIMESTAMP NOT NULL DEFAULT '0');" );
                this.addCheckTimeColumn( st );
                this.clearUserData( st );
                try ( ResultSet rs = st.executeQuery( "SELECT * FROM `Players`;" ) )
                {
                    int i = 0;
                    while ( rs.next() )
                    {
                        Config.getConfig().addUserToMap( rs.getString( "player" ).toLowerCase(), rs.getString( "ip" ) );
                        i++;
                    }
                    System.out.println( "[BotFilter] Белый список игроков успешно загружен (" + i + ")" );
                }
            }
        } catch ( SQLException | ClassNotFoundException ex )
        {
            ex.printStackTrace();
        }
    }

    public void close()
    {
        try
        {
            if ( this.connection != null && !this.connection.isClosed() )
            {
                this.executor.shutdownNow();
                this.stopSyncThread();
                this.connection.close();
            }
        } catch ( SQLException ex )
        {
            ex.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException, ClassNotFoundException
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
                    long currTime = System.currentTimeMillis();
                    String update = this.mysql
                            ? "INSERT INTO `Players` (`player`,`ip`,`check-time`)"
                            + " VALUES ('" + name + "','" + ip + "','" + currTime + "')"
                            + " ON DUPLICATE KEY UPDATE `ip`='" + ip + "', `check-time`='" + currTime + "';"
                            : "INSERT OR REPLACE INTO `Players` (`player`,`ip`,`check-time`)"
                            + " VALUES ('" + name + "','" + ip + "','" + currTime + "')";
                    statment.executeUpdate( update );
                } catch ( SQLException | ClassNotFoundException e )
                {
                    e.printStackTrace();
                }
            }
        } );
    }

    private void connect() throws SQLException, ClassNotFoundException
    {
        try
        {
            if ( this.connection != null )
            {
                this.connection.close();
            }
        } catch ( SQLException ignored )
        {
            ignored.printStackTrace();
        }

        System.out.println( "[SQL] Connect to " + ( this.host == null ? "sqlite" : this.host ) );
        long start = System.currentTimeMillis();
        if ( this.mysql )
        {
            this.connection = DriverManager.getConnection( "JDBC:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password );
        } else
        {
            Class.forName( "org.sqlite.JDBC" );
            this.connection = DriverManager.getConnection( "JDBC:sqlite:BotFilter/users.db" );
        }
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
                } catch ( SQLException | ClassNotFoundException ex )
                {
                    ex.printStackTrace();
                }
            }
        }, "DataBase sync" ) ).start();
    }

    private void stopSyncThread()
    {
        if ( syncThread != null && syncThread.isAlive() )
        {
            syncThread.interrupt();
        }
    }

    private boolean isColumnMissing(DatabaseMetaData metaData, String columnName) throws SQLException
    {
        try ( ResultSet rs = metaData.getColumns( null, null, "Players", columnName ) )
        {
            return !rs.next();
        }
    }

    private void addCheckTimeColumn(Statement st) throws SQLException
    {
        if ( isColumnMissing( connection.getMetaData(), "check-time" ) )
        {
            st.executeUpdate( "ALTER TABLE `Players` ADD COLUMN `check-time` TIMESTAMP NOT NULL DEFAULT '0';" );
            long currentTimestamp = System.currentTimeMillis();
            st.executeUpdate( "UPDATE `Players` SET `check-time` = " + currentTimestamp + ";" );
        }
    }

    private void clearUserData(Statement st) throws SQLException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add( Calendar.DATE, -purgeTime );
        long until = calendar.getTimeInMillis();
        int updatedRows = st.executeUpdate( "DELETE FROM `Players` WHERE `check-time` < " + until + ";" );
        System.out.println( "[BotFilter] Очищено " + updatedRows + " аккаунтов" );
    }

    public void mergeFromYml()
    {
        File userFile = new File( "BotFilter", "users.yml" );
        if ( userFile.exists() )
        {
            Configuration yaml;
            try
            {
                yaml = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( userFile );
                int i = 0;
                for ( String name : yaml.getKeys() )
                {
                    Config.getConfig().saveIp( name, yaml.getString( name ) );
                    i++;
                }
                System.out.println( "Перенесено " + i + " аккаунтов из yml хранилища в датабазу" );
                userFile.renameTo( new File( "BotFilter", "users.backup" ) );
            } catch ( IOException ex )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "Не могу загрузить users.yml", ex );
            }
        }
    }
}
