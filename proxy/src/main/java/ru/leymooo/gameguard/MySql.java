package ru.leymooo.gameguard;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ru.leymooo.gameguard.Config;

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

    public MySql(String host, String user, String password, String database, String port)
    {
        this.host = host;
        this.user = user;
        this.password = password;
        this.database = database;
        this.port = port;
        this.executor = Executors.newSingleThreadExecutor();
        try
        {
            this.connect();
            try ( Statement st = this.getConnection().createStatement() )
            {
                st.executeUpdate( "CREATE TABLE IF NOT EXISTS `Players` (`player` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE, `ip` VARCHAR(16) NOT NULL);" );
                st.close();
            }
            try ( Statement statement = this.getConnection().createStatement() )
            {
                ResultSet rs = statement.executeQuery( "SELECT * FROM `Players`;" );
                while ( rs.next() )
                {
                    Config.getConfig().addUserToMap( rs.getString( "player" ), rs.getString( "ip" ) );
                }
                System.out.println( "[GameGuard] Белый список игроков успешно загружен." );
                statement.close();
            }
        } catch ( SQLException ex )
        {
            ex.printStackTrace();
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
        this.executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                try ( Statement statment = getConnection().createStatement() )
                {
                    statment.executeUpdate( "INSERT INTO `Players` (`player`,`ip`) VALUES ('" + name + "','" + ip + "') ON DUPLICATE KEY UPDATE `ip`='" + ip + "';" );
                    statment.close();
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
        } catch ( Exception ignored )
        {
        }

        System.out.println( "[SQL] Connect to " + this.host );
        long start = System.currentTimeMillis();

        this.connection = DriverManager.getConnection( "JDBC:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password );
        System.out.println( "[SQL] Connected [" + ( System.currentTimeMillis() - start ) + " ms]" );
    }
}