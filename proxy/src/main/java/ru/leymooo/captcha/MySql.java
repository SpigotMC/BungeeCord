package ru.leymooo.captcha;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private String port;
    private final Object sosok;

    public MySql(String host, String user, String password, String database, String port)
    {
        this.sosok = new Object();
        this.host = host;
        this.user = user;
        this.password = password;
        this.database = database;
        this.port = port;
        this.executor = Executors.newSingleThreadExecutor();
        try
        {
            this.connect();
            MySql.this.getConnection().createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `Whitelist_new` ("
                    + "`Name` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE,"
                    + "`Ip` VARCHAR(16) NOT NULL);"
            );
        } catch ( SQLException ex )
        {
            ex.printStackTrace();
        }

        ( new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep( 500l );
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
                } catch ( InterruptedException ex )
                {
                }
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
        this.execute( "INSERT INTO `Whitelist_new` (`Name`,`Ip`) VALUES ('" + name + "','" + ip + "') ON DUPLICATE KEY UPDATE `Ip`='"+ip+"';" );
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

            this.connection = DriverManager.getConnection( "JDBC:mysql://" + this.host + ":"+this.port+"/" + this.database, this.user, this.password );
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
                        e.printStackTrace();
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
