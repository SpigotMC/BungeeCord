package ru.leymooo.botfilter.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.botfilter.BotFilter;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.config.Settings.SQL;

/**
 * @author Leymooo
 */
public class Sql
{

    private final BotFilter botFilter;
    private Connection connection;
    private boolean connecting = false;
    private long nextCleanUp = System.currentTimeMillis() + ( 60000 * 60 * 2 ); // + 2 hours

    private final ExecutorService executor = Executors.newFixedThreadPool( 2, new ThreadFactoryBuilder().setNameFormat( "BotFilter-SQL-%d" ).build() );
    private final Logger logger = BungeeCord.getInstance().getLogger();

    public Sql(BotFilter botFilter)
    {
        this.botFilter = botFilter;
        setupConnect();
    }

    private void setupConnect()
    {

        try
        {
            connecting = true;
            if ( executor.isShutdown() )
            {
                return;
            }
            if ( connection != null && connection.isValid( 3 ) )
            {
                return;
            }
            logger.info( "[BotFilter] Подключаюсь к датабазе..." );
            long start = System.currentTimeMillis();
            if ( Settings.IMP.SQL.STORAGE_TYPE.equalsIgnoreCase( "mysql" ) )
            {
                SQL s = Settings.IMP.SQL;
                connectToDatabase( String.format( "JDBC:mysql://%s:%s/%s?useSSL=false&useUnicode=true&characterEncoding=utf-8", s.HOSTNAME, String.valueOf( s.PORT ), s.DATABASE ), s.USER, s.PASSWORD );
            } else
            {
                Class.forName( "org.sqlite.JDBC" );
                connectToDatabase( "JDBC:sqlite:BotFilter/database.db", null, null );
            }
            logger.log( Level.INFO, "[BotFilter] Подключено ({0} мс)", System.currentTimeMillis() - start );
            createTable();
            clearOldUsers();
            loadUsers();
        } catch ( SQLException | ClassNotFoundException e )
        {
            logger.log( Level.WARNING, "Can not connect to database or execute sql: ", e );
            connection = null;
        } finally
        {
            connecting = false;
        }
    }

    private void connectToDatabase(String url, String user, String password) throws SQLException
    {
        this.connection = DriverManager.getConnection( url, user, password );
    }

    private void createTable() throws SQLException
    {
        String sql = "CREATE TABLE IF NOT EXISTS `Users` ("
                + "`Name` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE,"
                + "`Ip` VARCHAR(16) NOT NULL,"
                + "`LastCheck` BIGINT NOT NULL);";

        try ( PreparedStatement statement = connection.prepareStatement( sql ) )
        {
            statement.executeUpdate();
        }
    }

    private void clearOldUsers() throws SQLException
    {
        if ( Settings.IMP.SQL.PURGE_TIME <= 0 )
        {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add( Calendar.DATE, -Settings.IMP.SQL.PURGE_TIME );
        long until = calendar.getTimeInMillis();
        try ( PreparedStatement statement = connection.prepareStatement( "SELECT `Name` FROM `Users` WHERE `LastCheck` < " + until + ";" ) )
        {
            ResultSet set = statement.executeQuery();
            while ( set.next() )
            {
                botFilter.removeUser( set.getString( "Name" ) );
            }
        }
        if ( this.connection != null )
        {
            try ( PreparedStatement statement = connection.prepareStatement( "DELETE FROM `Users` WHERE `LastCheck` < " + until + ";" ) )
            {
                logger.log( Level.INFO, "[BotFilter] Очищено {0} аккаунтов", statement.executeUpdate() );
            }
        }
    }

    private void loadUsers() throws SQLException
    {
        try ( PreparedStatement statament = connection.prepareStatement( "SELECT * FROM `Users`;" );
                ResultSet set = statament.executeQuery() )
        {
            int i = 0;
            while ( set.next() )
            {
                String name = set.getString( "Name" );
                String ip = set.getString( "Ip" );
                if ( isInvalidName( name ) )
                {
                    removeUser( "REMOVE FROM `Users` WHERE `Ip` = '" + ip + "' AND `LastCheck` = '" + set.getLong( "LastCheck" ) + "';" );
                    continue;
                }
                botFilter.saveUser( name, IPUtils.getAddress( ip ) );
                i++;
            }
            logger.log( Level.INFO, "[BotFilter] Белый список игроков успешно загружен ({0})", i );
        }
    }

    private boolean isInvalidName(String name)
    {
        return name.contains( "'" ) || name.contains( "\"" );
    }

    private void removeUser(String sql)
    {
        if ( connection != null )
        {
            this.executor.execute( () ->
            {
                try ( PreparedStatement statament = connection.prepareStatement( sql ) )
                {
                    statament.execute();
                } catch ( SQLException ignored )
                {
                }
            } );
        }
    }

    public void saveUser(String name, String ip)
    {
        if ( connecting || isInvalidName( name ) )
        {
            return;
        }
        if ( connection != null )
        {
            this.executor.execute( () ->
            {
                final long timestamp = System.currentTimeMillis();
                String sql = "SELECT `Name` FROM `Users` where `Name` = '" + name + "' LIMIT 1;";
                try ( Statement statament = connection.createStatement();
                        ResultSet set = statament.executeQuery( sql ) )
                {
                    if ( !set.next() )
                    {
                        sql = "INSERT INTO `Users` (`Name`, `Ip`, `LastCheck`) VALUES ('" + name + "','" + ip + "','" + timestamp + "');";
                        statament.executeUpdate( sql );
                    } else
                    {
                        sql = "UPDATE `Users` SET `Ip` = '" + ip + "', `LastCheck` = '" + timestamp + "' where `Name` = '" + name + "';";
                        statament.executeUpdate( sql );
                    }
                } catch ( SQLException ex )
                {
                    logger.log( Level.WARNING, "[BotFilter] Не могу выполнить запрос к базе данных", ex );
                    logger.log( Level.WARNING, sql );
                    executor.execute( () -> setupConnect() );
                }
            } );
        }
    }

    public void tryCleanUP()
    {
        if ( Settings.IMP.SQL.PURGE_TIME > 0 && nextCleanUp - System.currentTimeMillis() <= 0 )
        {
            nextCleanUp = System.currentTimeMillis() + ( 60000 * 60 * 2 ); // + 2 hours
            try
            {
                clearOldUsers();
            } catch ( SQLException ex )
            {
                setupConnect();
                logger.log( Level.WARNING, "[BotFilter] Не могу очистить пользователей", ex );
            }
        }
    }

    public void close()
    {
        this.executor.shutdownNow();
        try
        {
            if ( connection != null )
            {
                this.connection.close();
            }
        } catch ( SQLException ignore )
        {
        }
        this.connection = null;
    }
}
