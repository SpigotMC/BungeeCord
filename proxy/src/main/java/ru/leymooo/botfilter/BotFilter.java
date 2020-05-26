package ru.leymooo.botfilter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import ru.leymooo.botfilter.caching.CachedCaptcha;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.caching.PacketUtils.KickType;
import ru.leymooo.botfilter.captcha.CaptchaGeneration;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.utils.GeoIp;
import ru.leymooo.botfilter.utils.ManyChecksUtils;
import ru.leymooo.botfilter.utils.ServerPingUtils;
import ru.leymooo.botfilter.utils.Sql;

/**
 * @author Leymooo
 */
public class BotFilter
{

    public static final long ONE_MIN = 60000;

    @Getter
    private final Map<String, Connector> connectedUsersSet = new ConcurrentHashMap<>();
    //UserName, Ip
    @Getter
    private final Map<String, BotFilterUser> userCache = new ConcurrentHashMap<>();

    private final ExecutorService executor;

    @Getter
    private final Sql sql;
    @Getter
    private final GeoIp geoIp;
    @Getter
    private final ServerPingUtils serverPingUtils;

    private final CheckState normalState;
    private final CheckState attackState;

    private int botCounter = 0;
    private long lastAttack = 0;
    @Setter
    @Getter
    private long lastCheck = System.currentTimeMillis();
    @Setter
    @Getter
    private boolean forceProtectionEnabled = false;

    public BotFilter(boolean startup)
    {
        Settings.IMP.reload( new File( "BotFilter", "config.yml" ) );
        DefinedPacket.fix_scoreboards = Settings.IMP.FIX_SCOREBOARDS;
        Scoreboard.DISABLE_DUBLICATE = Settings.IMP.FIX_SCOREBOARD_TEAMS;
        checkForUpdates( startup );
        if ( !CachedCaptcha.generated )
        {
            CaptchaGeneration.generateImages();
        }
        normalState = getCheckState( Settings.IMP.PROTECTION.NORMAL );
        attackState = getCheckState( Settings.IMP.PROTECTION.ON_ATTACK );
        PacketUtils.init();
        sql = new Sql( this );
        geoIp = new GeoIp( startup );
        serverPingUtils = new ServerPingUtils( this );

        if ( geoIp.isAvailable() )
        {
            executor = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() * 2, new ThreadFactoryBuilder().setNameFormat( "BF-%d" ).build() );
        } else
        {
            executor = null;
        }

        BotFilterThread.start();
    }

    public void disable()
    {
        BotFilterThread.stop();
        for ( Connector connector : connectedUsersSet.values() )
        {
            if ( connector.getUserConnection() != null )
            {
                connector.getUserConnection().disconnect( "§c[BotFilter] §aПерезагрузка фильтра" );
            }
            connector.setState( CheckState.FAILED );
        }
        connectedUsersSet.clear();
        geoIp.close();
        sql.close();
        ManyChecksUtils.clear();
        serverPingUtils.clear();
        if ( executor != null )
        {
            executor.shutdownNow();
        }
    }

    /**
     * Сохраняет игрока в памяти и в датебазе
     *
     * @param userName Имя игрока
     * @param address InetAddress игрока
     * @param afterCheck игрок после проверки или нет
     */
    public void saveUser(String userName, InetAddress address, boolean afterCheck)
    {
        userName = userName.toLowerCase();
        long timestamp = System.currentTimeMillis();
        BotFilterUser botFilterUser = userCache.get( userName );
        if ( botFilterUser == null )
        {
            botFilterUser = new BotFilterUser( userName, address.getHostAddress(), timestamp, timestamp );
        } else
        {
            botFilterUser.setIp( address.getHostAddress() );
            botFilterUser.setLastJoin( timestamp );
            if ( afterCheck )
            {
                botFilterUser.setLastCheck( timestamp );
            }
        }

        userCache.put( userName, botFilterUser );
        if ( sql != null )
        {
            sql.saveUser( botFilterUser );
        }
    }

    public void addUserToCache(BotFilterUser botFilterUser)
    {
        userCache.put( botFilterUser.getName(), botFilterUser );
    }

    /**
     * Удаляет игрока из памяти
     *
     * @param userName Имя игрока, которого следует удалить из памяти
     */
    public void removeUser(String userName)
    {
        userName = userName.toLowerCase();
        userCache.remove( userName );
    }

    public void connectToBotFilter(UserConnection userConnection)
    {
        userConnection.getCh().setEncoderProtocol( Protocol.GAME );
        userConnection.getCh().setDecoderProtocol( Protocol.BotFilter );
        Connector connector = new Connector( userConnection, this );

        if ( !addConnection( connector ) )
        {
            userConnection.disconnect( BungeeCord.getInstance().getTranslation( "already_connected_proxy" ) ); // TODO: Cache this disconnect packet
        } else
        {
            userConnection.getCh().getHandle().pipeline().get( HandlerBoss.class ).setHandler( connector );
            connector.spawn();
        }
    }

    /**
     * Добавляет игрока в мапу
     *
     * @param connector connector
     * @return если игрок был добавлен в мапу
     */
    public boolean addConnection(Connector connector)
    {
        return connectedUsersSet.putIfAbsent( connector.getName(), connector ) == null;
    }

    /**
     * Убирает игрока из мапы.
     *
     * @param name Имя игрока (lowercased)
     * @param connector Объект коннектора
     * @throws RuntimeException Имя игрока и коннектор null
     */
    public void removeConnection(String name, Connector connector)
    {
        name = name == null ? connector == null ? null : connector.getName() : name;
        if ( name != null )
        {
            connectedUsersSet.remove( name );
        } else
        {
            throw new RuntimeException( "Name and connector is null" );
        }
    }

    /**
     * Увеличивает счетчик ботов
     */
    public void incrementBotCounter()
    {
        botCounter++;
    }

    /**
     * Количество подключений на проверке
     *
     * @return количество подключений на проверке
     */
    public int getOnlineOnFilter()
    {
        return connectedUsersSet.size();
    }

    /**
     * Количество пользователей, которые прошли проверку
     *
     * @return количество пользователей, которые прошли проверку
     */
    public int getUsersCount()
    {
        return userCache.size();
    }

    /**
     * Проверяет нужно ли игроку проходить проверку
     *
     * @param userName Имя игрока
     * @param address InetAddress игрока
     * @return Нужно ли юзеру проходить проверку
     */
    public boolean needCheck(String userName, InetAddress address)
    {
        BotFilterUser botFilterUser = userCache.get( userName.toLowerCase() );
        return botFilterUser == null || ( Settings.IMP.FORCE_CHECK_ON_ATTACK && isUnderAttack() )
            || !botFilterUser.getIp().equalsIgnoreCase( address.getHostAddress() );
    }

    /**
     * Проверяет, находиться ли игрок на проверке
     *
     * @param name Имя игрока которого нужно искать на проверке
     * @return Находиться ли игрок на проверке
     */
    public boolean isOnChecking(String name)
    {
        return connectedUsersSet.containsKey( name.toLowerCase() );
    }

    /**
     * Проверяет есть ли в текущий момент бот атака
     *
     * @return true Если в текущий момент идёт атака
     */
    public boolean isUnderAttack()
    {
        if ( isForceProtectionEnabled() )
        {
            return true;
        }
        long currTime = System.currentTimeMillis();
        if ( currTime - lastAttack < Settings.IMP.PROTECTION_TIME )
        {
            return true;
        }
        long diff = currTime - lastCheck;
        if ( ( diff <= ONE_MIN ) && botCounter >= Settings.IMP.PROTECTION_THRESHOLD )
        {
            lastAttack = System.currentTimeMillis();
            lastCheck -= 61000;
            return true;
        } else if ( diff >= ONE_MIN )
        {
            botCounter = 0;
            lastCheck = System.currentTimeMillis();
        }
        return false;
    }

    public boolean checkBigPing(double ping)
    {
        int mode = isUnderAttack() ? 1 : 0;
        return ping != -1 && Settings.IMP.PING_CHECK.MODE != 2 && ( Settings.IMP.PING_CHECK.MODE == 0 || Settings.IMP.PING_CHECK.MODE == mode ) && ping >= Settings.IMP.PING_CHECK.MAX_PING;
    }

    public boolean isGeoIpEnabled()
    {
        int mode = isUnderAttack() ? 1 : 0;
        return geoIp.isAvailable() && ( Settings.IMP.GEO_IP.MODE == 0 || Settings.IMP.GEO_IP.MODE == mode );
    }

    public boolean checkGeoIp(InetAddress address)
    {

        return !geoIp.isAllowed( address );
    }

    public void checkAsyncIfNeeded(InitialHandler handler)
    {
        InetAddress address = handler.getAddress().getAddress();
        ChannelWrapper ch = handler.getCh();
        int version = handler.getVersion();
        BungeeCord bungee = BungeeCord.getInstance();
        if ( !Settings.IMP.PROTECTION.ALWAYS_CHECK && ManyChecksUtils.isManyChecks( address ) )
        {
            PacketUtils.kickPlayer( KickType.MANYCHECKS, Protocol.LOGIN, ch, version );
            bungee.getLogger().log( Level.INFO, "(BF) [{0}] disconnected: Too many checks in 10 min", address );
            return;
        }

        ServerPingUtils ping = getServerPingUtils();
        if ( ping.needCheck() && ping.needKickOrRemove( address ) )
        {
            PacketUtils.kickPlayer( KickType.PING, Protocol.LOGIN, ch, version );
            bungee.getLogger().log( Level.INFO, "(BF) [{0}] disconnected: The player did not ping the server", address.getHostAddress() );
            return;
        }

        /*if ( bungee.getConnectionThrottle() != null && bungee.getConnectionThrottle().throttle( address ) )
        {
            PacketUtils.kickPlayer( KickType.THROTTLE, Protocol.LOGIN, ch, version ); //BotFilter
            bungee.getLogger().log( Level.INFO, "[{0}] disconnected: Connection is throttled", address.getHostAddress() );
            return;
        }
         */
        if ( isGeoIpEnabled() )
        {
            executor.execute( () ->
            {
                if ( checkGeoIp( address ) )
                {
                    PacketUtils.kickPlayer( KickType.COUNTRY, Protocol.LOGIN, ch, version );
                    bungee.getLogger().log( Level.INFO, "(BF) [{0}] disconnected: Country is not allowed",
                            address.getHostAddress() );
                    return;
                }
                handler.delayedHandleOfLoginRequset();
            } );
        } else
        {
            handler.delayedHandleOfLoginRequset();
        }
    }

    public CheckState getCurrentCheckState()
    {
        return isUnderAttack() ? attackState : normalState;
    }

    private CheckState getCheckState(int mode)
    {
        switch ( mode )
        {
            case 0:
                return CheckState.ONLY_CAPTCHA;
            case 1:
                return CheckState.CAPTCHA_POSITION;
            case 2:
                return CheckState.CAPTCHA_ON_POSITION_FAILED;
            default:
                return CheckState.CAPTCHA_ON_POSITION_FAILED;
        }
    }

    private void checkForUpdates(boolean startup)
    {
        Logger logger = BungeeCord.getInstance().getLogger();
        try
        {
            logger.log( Level.INFO, "[BotFilter] Проверяю наличее обновлений" );
            URL url = new URL( "https://raw.githubusercontent.com/Leymooo/BungeeCord/master/version.txt" );
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout( 1200 );
            try ( BufferedReader in = new BufferedReader(
                    new InputStreamReader( conn.getInputStream() ) ) )
            {
                if ( !in.readLine().trim().equalsIgnoreCase( Settings.IMP.BOT_FILTER_VERSION ) )
                {

                    logger.log( Level.INFO, "§c[BotFilter] §aНайдена новая версия!" );
                    logger.log( Level.INFO, "§c[BotFilter] §aПожалуйста обновитесь!" );
                    logger.log( Level.INFO, "§c[BotFilter] §ahttp://rubukkit.org/threads/137038" );
                    if ( startup )
                    {
                        Thread.sleep( 3500L );
                    }
                } else
                {
                    logger.log( Level.INFO, "[BotFilter] Обновлений не найдено!" );
                }
            }
        } catch ( IOException | InterruptedException ex )
        {
            logger.log( Level.WARNING, "[BotFilter] Не могу проверить обновление", ex );
        }
    }

    public static enum CheckState
    {
        ONLY_POSITION,
        ONLY_CAPTCHA,
        CAPTCHA_POSITION,
        CAPTCHA_ON_POSITION_FAILED,
        SUCCESSFULLY,
        FAILED
    }
}
