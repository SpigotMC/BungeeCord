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
import java.util.logging.Logger;
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
    private Thread syncThread, refreshThread;
    private static final Logger logger = BungeeCord.getInstance().getLogger();

    public Sql(Configuration section)
    {
        this.mysql = "mysql".equalsIgnoreCase( section.getString( "type" ) );
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
                startSyncThread( section.getInt( "multibungee.sync-time", 10 ) );
            }
        }
        try
        {
            this.connect();
        } catch ( SQLException | ClassNotFoundException ex )
        {
            logger.log( Level.WARNING, "Не могу подкоючиться к базе данных", ex );
        }
        startRefreshThread();
    }

    public void loadUserData() throws SQLException, ClassNotFoundException
    {
        try ( Statement st = this.getConnection().createStatement() )
        {
            st.executeUpdate( "CREATE TABLE IF NOT EXISTS `Players` (`player` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE, `ip` VARCHAR(16) NOT NULL, `check-time` BIGINT NOT NULL DEFAULT '0');" );
            this.addCheckTimeColumn( st );
            this.clearUserData( st );
            Config.getConfig().getUsers().clear();
            try ( ResultSet rs = st.executeQuery( "SELECT * FROM `Players`;" ) )
            {
                int i = 0;
                while ( rs.next() )
                {
                    Config.getConfig().addUserToMap( rs.getString( "player" ).toLowerCase(), rs.getString( "ip" ) );
                    i++;
                }
                logger.log( Level.INFO, "[BotFilter] Белый список игроков успешно загружен ({0})", i );
            }
        }
    }

    public void fullClose()
    {
        try
        {
            if ( this.connection != null && !this.connection.isClosed() )
            {
                this.executor.shutdownNow();
                this.stopSyncThread();
                this.stopRefreshThread();
                this.connection.close();
            }
        } catch ( SQLException ex )
        {
            logger.log( Level.WARNING, "Не могу закрыть подключение к базе данных", ex );
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
                } catch ( SQLException | ClassNotFoundException ex )
                {
                    logger.log( Level.WARNING, "Не могу выполнить запрос к базе данных", ex );
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
        } catch ( SQLException ex )
        {
            logger.log( Level.WARNING, "Не могу закрыть подключение к базе данных", ex );
        }

        logger.log( Level.INFO, "[SQL] Connect to {0}", ( this.host == null ? "sqlite" : this.host ) );
        long start = System.currentTimeMillis();
        if ( this.mysql )
        {
            this.connection = DriverManager.getConnection( "JDBC:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password );
        } else
        {
            Class.forName( "org.sqlite.JDBC" );
            this.connection = DriverManager.getConnection( "JDBC:sqlite:BotFilter/users.db" );
        }
        logger.log( Level.INFO, "[SQL] Connected [{0} ms]", System.currentTimeMillis() - start );
    }

    private void startSyncThread(int syncTime)
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
                    logger.log( Level.WARNING, "Не могу синхронизировать аккаунты", ex );
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

    private void startRefreshThread()
    {
        stopRefreshThread();
        ( refreshThread = new Thread( () ->
        {
            while ( !Thread.interrupted() )
            {
                try
                {
                    Thread.sleep( 60 * 1000 * 60 * 12 ); //12 hours
                    this.loadUserData();
                } catch ( InterruptedException ex )
                {
                    return;
                } catch ( SQLException | ClassNotFoundException ex )
                {
                    logger.log( Level.WARNING, "Не могу загрузить аккаунты", ex );
                }
            }
        }, "UserData refresh thread" ) ).start();
    }

    private void stopRefreshThread()
    {
        if ( refreshThread != null && refreshThread.isAlive() )
        {
            refreshThread.interrupt();
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
            st.executeUpdate( "ALTER TABLE `Players` ADD COLUMN `check-time` BIGINT NOT NULL DEFAULT '0';" );
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
        logger.log( Level.INFO, "[BotFilter] Очищено {0} аккаунтов", updatedRows );
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
                logger.log( Level.INFO, "Перенесено {0} аккаунтов из yml хранилища в датабазу", i );
                userFile.renameTo( new File( "BotFilter", "users.backup" ) );
            } catch ( IOException ex )
            {
                logger.log( Level.WARNING, "Не могу загрузить users.yml", ex );
            }
        }
    }
}
