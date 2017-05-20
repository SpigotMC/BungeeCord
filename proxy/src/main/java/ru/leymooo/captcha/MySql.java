package ru.leymooo.captcha;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Скорее всего Yooxa)
 */
public class MySql
{

    private Connection connection;
    private ExecutorService executor;
    private String host;
    private String database;
    private String user;
    private String password;
    private final Object sosok;

    public MySql(String host, String user, String password, String database)
    {
        this.sosok = new Object();
        this.host = host;
        this.user = user;
        this.password = password;
        this.database = database;
        this.executor = Executors.newSingleThreadExecutor();
        this.execute(
                "CREATE TABLE IF NOT EXISTS `Whitelist_new`"
                + " ("
                + "`Name` VARCHAR(24) AUTO_INCREMENT PRIMARY KEY NOT NULL UNIQUE,"
                + "`Ip` VARCHAR(16) NOT NULL"
                + ")"
        );
        ( new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                Statement statement = null;
                int i = 0;
                try
                {
                    statement = getConnection().createStatement();
                    ResultSet rs = statement.executeQuery( "SELECT * FROM `Whitelist_new`;" );
                    while ( rs.next() )
                    {
                        Configuration.getInstance().addUserToMap( rs.getString( "Name" ), rs.getString( "Ip" ) );
                        i++;
                    }
                    rs.close();
                } catch ( SQLException ex )
                {
                    ex.printStackTrace();
                } finally
                {
                    if ( statement != null )
                    {
                        try
                        {
                            statement.close();
                        } catch ( SQLException ex )
                        {
                        }
                    }
                }
                System.out.println( "[Captcha] Загружено " + i + " адресов" );
            }

        }, "Captcha SqlStorage" ) ).start();
    }

    private Connection getConnection() throws SQLException
    {
        synchronized ( this.sosok )
        {
            if ( this.connection == null || this.connection.isClosed() )
            {
                this.connect();
            }

            return this.connection;
        }
    }

    public void addAddress(String name, String ip)
    {
        this.execute( "INSERT INTO `Whitelist_new` (`Name`,`Ip`) VALUES ('" + name + "','" + ip + "') ON DUPLICATE KEY UPDATE `Ip`=`Ip`;" );
    }

    private void close() throws SQLException
    {
        this.connection.close();
        this.executor.shutdownNow();
    }

    private void connect() throws SQLException
    {
        synchronized ( this.sosok )
        {
            try
            {
                if ( this.connection != null )
                {
                    this.connection.close();
                }
            } catch ( Exception exception )
            {
                ;
            }

            System.out.println( "[SQL] Connect to " + this.host );
            long start = System.currentTimeMillis();

            this.connection = DriverManager.getConnection( "JDBC:mysql://" + this.host + ":3306/" + this.database, this.user, this.password );
            System.out.println( "[SQL] Connected [" + ( System.currentTimeMillis() - start ) + " ms]" );
        }
    }

    private void execute(final String sql)
    {
        this.executor.execute( new Runnable()
        {
            public void run()
            {
                try
                {
                    Statement statment = MySql.this.getConnection().createStatement();

                    try
                    {
                        statment.executeUpdate( sql );
                        statment.close();
                    } catch ( SQLException e )
                    {
                    } finally
                    {
                        if ( statment != null && !statment.isClosed() )
                        {
                            try
                            {
                                statment.close();
                            } catch ( SQLException e )
                            {
                            }
                        }
                    }
                } catch ( SQLException sqlexception )
                {
                    sqlexception.printStackTrace();
                }

            }
        } );
    }
}
