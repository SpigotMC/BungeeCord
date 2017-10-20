package ru.leymooo.botfilter;

import ru.leymooo.botfilter.utils.GeoIpUtils;
import ru.leymooo.botfilter.utils.Utils;
import ru.leymooo.botfilter.utils.Proxy;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import lombok.Data;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeTitle;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.BaseComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.extra.TimeUpdate;
import ru.leymooo.botfilter.utils.ServerPingUtils;
import ru.leymooo.fakeonline.FakeOnline;

/**
 *
 * @author Leymooo
 */
@Data
public class Config
{

    /* Добро пожаловать в гору говнокода и костылей */
    //TODO: Дописать проверку по пингу сервера
    @Getter
    private static Config config;
    private String check = "msg-check", check2 = "msg-check-2", checkSus = "msg-check-sus", errorManyChecks = "error-many-checks",
            errorBot = "error-not-a-player", errorProxy = "error-proxy-detected", errorConutry = "error-country-not-allowed",
            errorPackets = "error-many-pos-packets", errorWrongButton = "error-wrong-button", errorCantUse = "error-cannot-use-button",
            errorNotPressed = "error-button-not-pressed", actionBar = "action-bar", bigPing = "error-ping-is-too-big";
    private boolean mySqlEnabled = false, permanent = false, forceKick = true,
            buttonNormal = true, buttonPermanent = true, buttonOnAttack = true,
            onlineFromFilter = true;
    private int maxChecksPer1min = 30, protectionTime = 120000, maxPing = -1;
    private MySql mysql = null;
    private GeoIpUtils geoUtils;
    private final HashMap<String, String> users = new HashMap<>();
    private final Set<BFConnector> connectedUsersSet = Sets.newConcurrentHashSet();
    private Configuration userData, mainConfig;
    private File dataFile = new File( "BotFilter", "users.yml" );
    private double attackStartTime = 0, lastBotAttackCheck = System.currentTimeMillis();
    private AtomicInteger botCounter = new AtomicInteger();
    private Proxy proxy;
    private static Thread t;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    public Config()
    {
        this.startThread();
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
            this.mysql = new MySql( this.mainConfig.getSection( "mysql" ) );
        }
    }

    public void addUserToMap(String name, String ip)
    {
        if ( this.users.containsKey( name ) )
        {
            this.users.replace( name, ip );
        } else
        {
            this.users.put( name, ip );
        }
    }

    public void saveIp(String name, String ip)
    {
        if ( this.mySqlEnabled )
        {
            this.mysql.addAddress( name.toLowerCase(), ip );
        }
        this.addUserToMap( name.toLowerCase(), ip );
        if ( this.userData != null )
        {
            this.userData.set( name.toLowerCase(), ip );
            executor.execute( () ->
            {
                try
                {
                    ConfigurationProvider.getProvider( YamlConfiguration.class ).save( this.userData, this.dataFile );
                } catch ( IOException e )
                {
                    BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not save user file", e );
                }
            } );
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

    public boolean isProtectionEnabled()
    {
        return isPermanent() || isUnderAttack();
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
            this.lastBotAttackCheck -= 61000;
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

    public boolean needButtonCheck()
    {
        return buttonNormal || ( buttonPermanent && isPermanent() ) || ( buttonOnAttack && isUnderAttack() );
    }

    private void load(Configuration config)
    {
        this.geoUtils = new GeoIpUtils( new File( "BotFilter" ), config.getStringList( "allowed-countries-auto" ), config.getStringList( "allowed-countries-permanent" ) );
        this.proxy = new Proxy( new File( "BotFilter" ), config.getSection( "proxy" ) );
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
        this.errorNotPressed = ChatColor.translateAlternateColorCodes( '&', config.getString( errorNotPressed ) );
        this.actionBar = ChatColor.translateAlternateColorCodes( '&', config.getString( actionBar ) );
        this.bigPing = ChatColor.translateAlternateColorCodes( '&', config.getString( bigPing ) );
        this.mySqlEnabled = config.getBoolean( "mysql.enabled" );
        this.permanent = config.getBoolean( "permanent-protection" );
        this.maxChecksPer1min = config.getInt( "max-checks-per-1-min" );
        this.protectionTime = config.getInt( "protection-time" ) * 1000;
        this.maxPing = config.getInt( "kick-if-ping-more-than" );
        this.forceKick = config.getBoolean( "force-kick-bots-on-join-if-attack-detected" );
        this.buttonNormal = config.getBoolean( "button-check.on-normal-mode" );
        this.buttonOnAttack = config.getBoolean( "button-check.on-bot-attack" );
        this.buttonPermanent = config.getBoolean( "button-check.on-permanent-protection" );
        this.onlineFromFilter = config.getBoolean( "show-filter-online" );
        new FakeOnline( config.getBoolean( "fake-online.enabled" ), config.getSection( "fake-online.booster" ) );
        new ServerPingUtils( config.getSection( "server-ping-check" ) );
        BFConnector.chat = new Chat( ComponentSerializer.toString( TextComponent.fromLegacyText( getCheck() ) ), (byte) ChatMessageType.CHAT.ordinal() );
        BFConnector.timeUpdate = new TimeUpdate( 1, config.getInt( "world-time" ) );
        String[] checkTitle = ChatColor.translateAlternateColorCodes( '&', config.getString( "msg-check-title" ) ).split( ";" );
        BFConnector.checkTitle = new BungeeTitle().title( TextComponent.fromLegacyText( checkTitle[0] ) )
                .subTitle( TextComponent.fromLegacyText( checkTitle[1] ) ).fadeIn( 5 ).fadeOut( 1 ).stay( 320 /*16 сек*/ );

        String[] checkTitleSus = ChatColor.translateAlternateColorCodes( '&', config.getString( "msg-check-title-sus" ) ).split( ";" );
        BFConnector.susCheckTitle = new BungeeTitle().title( TextComponent.fromLegacyText( checkTitleSus[0] ) )
                .subTitle( TextComponent.fromLegacyText( checkTitleSus[1] ) ).fadeIn( 10 ).fadeOut( 10 ).stay( 25 );

        String[] checkTitleButton = ChatColor.translateAlternateColorCodes( '&', config.getString( "msg-press-button-title" ) ).split( ";" );
        BFConnector.buttonCheckTitle = new BungeeTitle().title( TextComponent.fromLegacyText( checkTitleButton[0] ) )
                .subTitle( TextComponent.fromLegacyText( checkTitleButton[1] ) ).fadeIn( 5 ).fadeOut( 10 ).stay( 60 );
    }

    private Configuration checkFileAndGiveConfig() throws IOException
    {
        File dataFolder = new File( "BotFilter" );
        dataFolder.mkdir();
        File file = new File( dataFolder, "config.yml" );
        if ( file.exists() )
        {
            return ConfigurationProvider.getProvider( YamlConfiguration.class ).load( file );
        }
        InputStream in = getClass().getClassLoader().getResourceAsStream( ( "bfconfig.yml" ) );
        Files.copy( in, file.toPath() );
        return ConfigurationProvider.getProvider( YamlConfiguration.class ).load( file );
    }

    private void checkAndUpdateConfig()
    {
        if ( mainConfig.getInt( "config-version" ) != 5 )
        {
            File configFile = new File( "BotFilter", "config.yml" );
            try
            {
                File oldFile = new File( "BotFilter", "config-old.yml" );
                if ( oldFile.exists() )
                {
                    oldFile.delete();
                }
                if ( configFile.renameTo( oldFile ) )
                {

                    this.mainConfig = checkFileAndGiveConfig();
                    BungeeCord.getInstance().getLogger().info( "§cВНИМАНИЕ! §aБыл создан новый конфиг." );
                    BungeeCord.getInstance().getLogger().info( "§cВНИМАНИЕ! §aСтрарый конфиг сохранён под именем - §bconfig-old.yml§a." );
                    BungeeCord.getInstance().getLogger().info( "§cВНИМАНИЕ! §aЕсли вы чтото изменяли то, не забудьте изменить заного." );
                    BungeeCord.getInstance().getLogger().info( "§aЗапуск через §c10 §aсекунд." );
                    Thread.sleep( 10000L );
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
        ( t = new Thread( () ->
        {
            while ( !Thread.interrupted() )
            {
                try
                {
                    Thread.sleep( 700L );
                } catch ( InterruptedException ex )
                {
                    return;
                }
                long currTime = System.currentTimeMillis();
                lastBotAttackCheck = Config.getConfig().getConnectedUsersSet().size() >= ( maxChecksPer1min / 4 ) && !isUnderAttack()
                        ? currTime : lastBotAttackCheck;
                for ( BFConnector connector : Config.getConfig().getConnectedUsersSet() )
                {
                    if ( connector.isConnected() )
                    {
                        Utils.CheckState state = connector.getState();
                        connector.sendCheckMessage( state == Utils.CheckState.BUTTON ? check2 : check );
                        connector.sendKeepAlive();
                        switch ( state )
                        {
                            case FAILED:
                                connector.getConnection().disconnect( errorBot );
                                break;
                            case BUTTON:
                                long buttonTime = System.currentTimeMillis() - connector.getButtonCheckStart();
                                if ( buttonTime >= 15000 )
                                {
                                    connector.getConnection().disconnect( errorNotPressed );
                                }
                                break;
                            case POSITION:
                                long posTime = System.currentTimeMillis() - connector.getJoinTime();
                                if ( posTime >= 20000 )
                                {
                                    connector.getConnection().disconnect( errorBot );
                                    continue;
                                }
                                connector.sendCheckPackets( true );
                                break;
                        }
                    }
                }
            }
        }, "BotFilter thread" ) ).start();
    }
}
