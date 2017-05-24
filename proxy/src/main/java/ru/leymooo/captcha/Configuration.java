package ru.leymooo.captcha;

import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 *
 * @author Leymooo
 */
public class Configuration
{

    private static Configuration conf;
    @Getter
    private String wrongCaptchaKick = "wrong-captcha-kick";
    @Getter
    private String timeOutKick = "timeout-kick";
    @Getter
    private String botKick = "bot-kick";
    @Getter
    private String wrongCaptcha = "wrong-captcha";
    @Getter
    private String enterCaptcha = "enter-captcha";
    @Getter
    private boolean ripple = true;
    @Getter
    private boolean blur = true;
    @Getter
    private boolean outline = false;
    @Getter
    private boolean rotate = true;
    @Getter
    @Setter
    private boolean underAttack = false;
    @Getter
    private boolean capthcaAfterReJoin = false;
    private boolean mySqlEnabled = false;
    @Getter
    private int worldType;
    @Getter
    private int mode = 0;
    @Getter
    private int threads = 4;
    @Getter
    private int timeout = 15;
    @Getter
    private int maxCaptchas = 1500;
    @Getter
    private int underAttackTime = 120000;
    private MySql mysql = null;
    private final HashMap<String, String> users = new HashMap<>();
    @Getter
    private final Set<CaptchaConnector> connectedUsersSet = Sets.newConcurrentHashSet();
    //=========Такая реализация скорее всего лучше, чем использование thread======//
    private double attactStartTime = 0;
    private double lastBotAttackCheck = System.currentTimeMillis();
    private AtomicInteger botCounter = new AtomicInteger();

    public Configuration()
    {
        conf = this;
        net.md_5.bungee.config.Configuration config = null;
        try
        {
            config = checkFileAndGiveConfig();
            this.load( config );
        } catch ( IOException e )
        {
            BungeeCord.getInstance().getLogger().log( Level.WARNING, "Please write me about this error(vk.com/Leymooo_s)", e);
            System.exit( 0 );
        }
        if ( mySqlEnabled && !capthcaAfterReJoin )
        {
            this.mysql = new MySql( config.getString( "mysql.host" ), config.getString( "mysql.username" ), config.getString( "mysql.password" ), config.getString( "mysql.database" ), config.getString( "mysql.port", "3306" ) );
        }
        this.startThread();
    }

    public static Configuration getInstance()
    {
        return conf;
    }

    public void addUserToMap(String name, String ip)
    {
        this.users.put( name.toLowerCase(), ip );
    }

    public void saveIp(String name, String ip)
    {
        if ( this.capthcaAfterReJoin )
        {
            return;
        }
        if ( this.mySqlEnabled )
        {
            this.mysql.addAddress( name.toLowerCase(), ip );
        }
        this.addUserToMap( name, ip );
    }

    public boolean needCapthca(String name, String ip)
    {
        if ( this.capthcaAfterReJoin )
        {
            return true;
        }
        //Проверяем включён ли режим 'под атакой'
        if ( System.currentTimeMillis() - this.attactStartTime < this.underAttackTime )
        {
            return true;
        }
        //Проверяем что не прошло 5 секунд после последней проверки на бот атаку и проверяем есть ли бот атака.
        if ( ( System.currentTimeMillis() - this.lastBotAttackCheck <= 5000 ) && this.botCounter.incrementAndGet() >= 130 )
        {
            this.attactStartTime = System.currentTimeMillis();
            this.lastBotAttackCheck = System.currentTimeMillis();
            return true;
        }
        this.botCounter.incrementAndGet();
        if ( System.currentTimeMillis() - this.lastBotAttackCheck >= 5000 )
        {
            this.lastBotAttackCheck = System.currentTimeMillis();
            this.botCounter.set( 0 );
        }
        if ( !this.users.containsKey( name.toLowerCase() ) )
        {
            return true;
        }
        return !this.users.get( name.toLowerCase() ).equalsIgnoreCase( ip );
    }

    private void load(net.md_5.bungee.config.Configuration config)
    {
        this.wrongCaptchaKick = ChatColor.translateAlternateColorCodes( '&', config.getString( wrongCaptchaKick ) );
        this.timeOutKick = ChatColor.translateAlternateColorCodes( '&', config.getString( timeOutKick ) );
        this.botKick = ChatColor.translateAlternateColorCodes( '&', config.getString( botKick ) );
        this.wrongCaptcha = ChatColor.translateAlternateColorCodes( '&', config.getString( wrongCaptcha ) );
        this.enterCaptcha = ChatColor.translateAlternateColorCodes( '&', config.getString( enterCaptcha ) );
        this.mySqlEnabled = config.getBoolean( "mysql.enabled" );
        this.capthcaAfterReJoin = config.getBoolean( "always-captcha-enter" );
        this.ripple = config.getBoolean( "captcha-generator-settings.outline" );
        this.blur = config.getBoolean( "captcha-generator-settings.blur" );
        this.outline = config.getBoolean( "captcha-generator-settings.outline" );
        this.rotate = config.getBoolean( "captcha-generator-settings.rotate" );
        this.maxCaptchas = config.getInt( "max-captchas" );
        this.mode = config.getInt( "captcha-generator" );
        this.timeout = config.getInt( "max-enter-time" ) * 1000;
        this.underAttackTime = config.getInt( "under-attack-time" ) * 1000;
        this.threads = config.getInt( "image-generation-threads" );
        this.worldType = config.getInt( "world-type" );
    }

    private net.md_5.bungee.config.Configuration checkFileAndGiveConfig() throws IOException
    {
        File file = new File( "captcha.yml" );
        if ( file.exists() )
        {
            return ConfigurationProvider.getProvider( YamlConfiguration.class ).load( file );
        }
        InputStream in = getClass().getClassLoader().getResourceAsStream( ( "captcha.yml" ) );
        Files.copy( in, file.toPath() );
        return ConfigurationProvider.getProvider( YamlConfiguration.class ).load( file );
    }

    private void startThread()
    {
        ( new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                while ( true )
                {
                    try
                    {
                        Thread.sleep( 1000L );
                    } catch ( InterruptedException ex )
                    {
                    }
                    for ( CaptchaConnector connector : getConnectedUsersSet() )
                    {
                        if (connector.getUserConnection() == null) {
                            getConnectedUsersSet().remove( connector );
                            continue;
                        }
                        if ( connector.isBot() )
                        {
                            connector.getUserServer().kick( getBotKick() );
                            continue;
                        }
                        if ( System.currentTimeMillis() - connector.getJoinTime() >= getTimeout() )
                        {
                            connector.getUserServer().kick( getTimeOutKick() );
                            continue;
                        }
                        connector.getUserServer().enterCapthca();
                    }
                }
            }
        }, "Captcha Thread" ) ).start();
    }
}
