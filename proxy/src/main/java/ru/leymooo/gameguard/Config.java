package ru.leymooo.gameguard;

import ru.leymooo.gameguard.utils.GeoIpUtils;
import ru.leymooo.gameguard.utils.Utils;
import ru.leymooo.gameguard.utils.Proxy;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
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
    private String check2 = "msg-check-2";
    private String checkSus = "msg-check-sus";
    private String errorManyChecks = "error-many-checks";
    private String errorBot = "error-not-a-player";
    private String errorProxy = "error-proxy-detected";
    private String errorConutry = "error-country-not-allowed";
    private String errorPackets = "error-many-pos-packets";
    private String errorWrongButton = "error-wrong-button";
    private String errorCantUse = "error-cannot-use-button";
    private boolean mySqlEnabled = false;
    private boolean permanent = false;
    private boolean forceKick = true;
    private int maxChecksPer1min = 30;
    private int protectionTime = 120000;
    private MySql mysql = null;
    private GeoIpUtils geoUtils;
    private final HashMap<String, String> users = new HashMap<>();
    private final Set<GGConnector> connectedUsersSet = Sets.newConcurrentHashSet();
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
            this.checkAndUpdateConfig();
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
        if ( System.currentTimeMillis() - this.attackStartTime < this.protectionTime )
        {
            return true;
        }
        if ( ( System.currentTimeMillis() - this.lastBotAttackCheck <= 60000 ) && this.botCounter.get() >= this.maxChecksPer1min )
        {
            this.attackStartTime = System.currentTimeMillis();
            this.lastBotAttackCheck += 61000;
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
        this.check2 = ChatColor.translateAlternateColorCodes( '&', config.getString( check2 ) );
        this.checkSus = ChatColor.translateAlternateColorCodes( '&', config.getString( checkSus ) );
        this.errorManyChecks = ChatColor.translateAlternateColorCodes( '&', config.getString( errorManyChecks ) );
        this.errorBot = ChatColor.translateAlternateColorCodes( '&', config.getString( errorBot ) );
        this.errorProxy = ChatColor.translateAlternateColorCodes( '&', config.getString( errorProxy ) );
        this.errorConutry = ChatColor.translateAlternateColorCodes( '&', config.getString( errorConutry ) );
        this.errorPackets = ChatColor.translateAlternateColorCodes( '&', config.getString( errorPackets ) );
        this.errorWrongButton = ChatColor.translateAlternateColorCodes( '&', config.getString( errorWrongButton ) );
        this.errorCantUse = ChatColor.translateAlternateColorCodes( '&', config.getString( errorCantUse ) );
        this.mySqlEnabled = config.getBoolean( "mysql.enabled" );
        this.permanent = config.getBoolean( "permanent-protection" );
        this.maxChecksPer1min = config.getInt( "max-checks-per-1-min" );
        this.protectionTime = config.getInt( "protection-time" ) * 1000;
        this.forceKick = config.getBoolean( "force-kick-bots-on-join-if-attack-detected" );
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

    private void checkAndUpdateConfig()
    {
        if ( mainConfig.getInt( "config-version" ) != 1 )
        {
            File configFile = new File( "GameGuard", "gameguard.yml" );
            try
            {
                if ( configFile.renameTo( new File( "GameGuard", "gameguard-old.yml" ) ) )
                {

                    this.mainConfig = checkFileAndGiveConfig();
                    BungeeCord.getInstance().getLogger().info( "§cВНИМАНИЕ! §aБыл создан новый конфиг." );
                    BungeeCord.getInstance().getLogger().info( "§cВНИМАНИЕ! §aСтрарый конфиг сохранён под именем - §bgameguard-old.yml§a." );
                    BungeeCord.getInstance().getLogger().info( "§cВНИМАНИЕ! §aЕсли вы чтото изменяли то, не забудьте изменить заного." );
                    BungeeCord.getInstance().getLogger().info( "§aЗапуск через §c10 §aсекунд." );
                    Thread.sleep( 10000l );
                    return;
                }
                throw new InterruptedException();
            } catch ( IOException | InterruptedException e )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "Не могу создать новый конфиг. Удалите конфиг вручную.", e );
                System.exit( 0 );
            }
        }
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
                    long currTime = System.currentTimeMillis();
                    lastBotAttackCheck = Config.getConfig().getConnectedUsersSet().size() >= ( maxChecksPer1min / 4 ) && !isUnderAttack()
                            ? currTime : lastBotAttackCheck;
                    for ( GGConnector connector : Config.getConfig().getConnectedUsersSet() )
                    {
                        if ( connector.isConnected() )
                        {
                            Utils.CheckState state = connector.getState();
                            connector.getConnection().sendMessages( state == Utils.CheckState.BUTTON ? check2 : check );
                            long diff = currTime - connector.getJoinTime();
                            if ( ( diff >= 9000 && state == Utils.CheckState.POSITION )
                                    || ( connector.getState() == Utils.CheckState.FAILED )
                                    || ( diff >= 30000 && state == Utils.CheckState.BUTTON ) )
                            {
                                connector.getConnection().disconnect( errorBot );
                            }
                        }
                    }
                }
            }
        }, "Captcha Thread" ) ).start();
    }
}
