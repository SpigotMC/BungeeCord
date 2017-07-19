package ru.leymooo.gameguard;

import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import lombok.Data;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 *
 * @author Leymooo
 */
@Data
public class Config
{
    /* Добро пожаловать в гору говнокода и костылей */
    @Getter
    private static Config config;
    private String check = "msg-check";
    private String checkSus = "msg-check-sus";
    private String error0 = "error-many-checks";
    private String error1 = "error-not-a-player";
    private String error2 = "error-proxy-detected";
    private String error2_1 = "error-country-not-allowed";
    private String error3 = "error-many-pos-packets";
    private boolean mySqlEnabled = false;
    private boolean permanent = false;
    private int maxChecksPer1min = 30;
    private int protectionTime = 120000;
    private MySql mysql = null;
    private GeoIpUtils geoUtils;
    private final HashMap<String, String> users = new HashMap<>();
    private final Set<GGConnector> connectedUsersSet = Sets.newConcurrentHashSet();
    private boolean redisBungee = false;
    private Configuration userData;
    private Configuration mainConfig;
    private File dataFile = new File( "GameGuard", "users.yml" );
    private double attackStartTime = 0;
    private double lastBotAttackCheck = System.currentTimeMillis();
    private AtomicInteger botCounter = new AtomicInteger();
    private Proxy proxy;
    private static Thread t;

    public Config()
    {
        config = this;
        try
        {
            this.mainConfig = checkFileAndGiveConfig();
            this.load( this.mainConfig );

            if ( !this.mySqlEnabled )
            {
                if ( !this.dataFile.exists() )
                {
                    this.dataFile.createNewFile();
                }
                this.userData = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( this.dataFile );
                loadUsers();
            }
        } catch ( IOException e )
        {
            BungeeCord.getInstance().getLogger().log( Level.WARNING, "Please write me about this error(vk.com/Leymooo_s)", e );
            System.exit( 0 );
        }
        if ( this.mySqlEnabled )
        {
            this.mysql = new MySql( this.mainConfig.getString( "mysql.host" ), this.mainConfig.getString( "mysql.username" ), this.mainConfig.getString( "mysql.password" ), this.mainConfig.getString( "mysql.database" ), this.mainConfig.getString( "mysql.port", "3306" ) );
        }
        this.startThread();
    }

    public void addUserToMap(String name, String ip)
    {
        this.users.put( name.toLowerCase(), ip );
    }

    public void saveIp(String name, String ip)
    {
        if ( this.mySqlEnabled )
        {
            this.mysql.addAddress( name.toLowerCase(), ip );
        }
        this.addUserToMap( name, ip );
        if ( this.userData != null )
        {
            this.userData.set( name.toLowerCase(), ip );
            try
            {
                ConfigurationProvider.getProvider( YamlConfiguration.class ).save( this.userData, this.dataFile );
            } catch ( IOException e )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not save user file", e );
            }
        }
    }

    private void loadUsers()
    {
        if ( this.userData != null )
        {
            for ( String name : this.userData.getKeys() )
            {
                this.addUserToMap( name.toLowerCase(), this.userData.getString( name ) );
            }
        }
    }

    public boolean isUnderAttack()
    {
        if ( isPermanent() )
        {
            return true;
        }
        if ( System.currentTimeMillis() - this.attackStartTime < this.protectionTime )
        {
            return true;
        }
        if ( ( System.currentTimeMillis() - this.lastBotAttackCheck <= 60000 ) && this.botCounter.get() >= this.maxChecksPer1min )
        {
            this.attackStartTime = System.currentTimeMillis();
            this.lastBotAttackCheck = System.currentTimeMillis();
            return true;
        }
        if ( System.currentTimeMillis() - this.lastBotAttackCheck >= 60000 )
        {
            this.lastBotAttackCheck = System.currentTimeMillis();
            this.botCounter.set( 0 );
        }
        return false;
    }

    public boolean needCheck(String name, String ip)
    {
        return !( this.users.containsKey( name.toLowerCase() ) && this.users.get( name.toLowerCase() ).equalsIgnoreCase( ip ) );
    }

    private void load(Configuration config)
    {
        this.geoUtils = new GeoIpUtils( new File( "GameGuard" ), config.getStringList( "allowed-countries-auto" ), config.getStringList( "allowed-countries-permanent" ) );
        this.proxy = new Proxy( new File( "GameGuard" ) );
        this.check = ChatColor.translateAlternateColorCodes( '&', config.getString( check ) );
        this.checkSus = ChatColor.translateAlternateColorCodes( '&', config.getString( checkSus ) );
        this.error0 = ChatColor.translateAlternateColorCodes( '&', config.getString( error0 ) );
        this.error1 = ChatColor.translateAlternateColorCodes( '&', config.getString( error1 ) );
        this.error2 = ChatColor.translateAlternateColorCodes( '&', config.getString( error2 ) );
        this.error2_1 = ChatColor.translateAlternateColorCodes( '&', config.getString( error2_1 ) );
        this.error3 = ChatColor.translateAlternateColorCodes( '&', config.getString( error3 ) );
        this.mySqlEnabled = config.getBoolean( "mysql.enabled" );
        this.permanent = config.getBoolean( "permanent-protection" );
        this.maxChecksPer1min = config.getInt( "max-checks-per-1-min" );
        this.protectionTime = config.getInt( "protection-time" ) * 1000;
        // new FakeOnline( config.getBoolean( "fake-online.enabled" ), config.getSection( "fake-online.booster" ) );
    }

    private Configuration checkFileAndGiveConfig() throws IOException
    {
        File dataFolder = new File( "GameGuard" );
        dataFolder.mkdir();
        File file = new File( dataFolder, "gameguard.yml" );
        if ( file.exists() )
        {
            return ConfigurationProvider.getProvider( YamlConfiguration.class ).load( file );
        }
        InputStream in = getClass().getClassLoader().getResourceAsStream( ( "gameguard.yml" ) );
        Files.copy( in, file.toPath() );
        return ConfigurationProvider.getProvider( YamlConfiguration.class ).load( file );
    }

    private void startThread()
    {
        if ( t != null && t.isAlive() )
        {
            t.interrupt();
        }
        ( t = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                while ( true )
                {
                    try
                    {
                        Thread.sleep( 2000L );
                    } catch ( InterruptedException ex )
                    {
                    }
                    if ( Config.getConfig().getConnectedUsersSet().size() >= 7 && !isUnderAttack())
                    {
                        //Обнулим навсякий случай счётчик если онлайн больше 6.
                        lastBotAttackCheck = System.currentTimeMillis();
                    }
                    long currTime = System.currentTimeMillis();
                    for ( GGConnector connector : Config.getConfig().getConnectedUsersSet() )
                    {
                        if ( connector.isConnected() )
                        {
                            connector.getConnection().sendMessage( check );
                            if ( currTime - connector.getJoinTime() >= 8500 )
                            {
                                connector.getConnection().disconnect( error1 );
                            }
                        } else
                        {
                            Config.getConfig().getConnectedUsersSet().remove( connector );
                        }
                    }
                }
            }
        }, "Captcha Thread" ) ).start();
    }
}
