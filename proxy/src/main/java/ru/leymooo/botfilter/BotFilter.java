package ru.leymooo.botfilter;

import java.io.BufferedReader;
import ru.leymooo.botfilter.utils.Sql;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.botfilter.caching.CachedCaptcha;
import ru.leymooo.botfilter.utils.GeoIp;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.caching.PacketUtils.KickType;
import ru.leymooo.botfilter.captcha.CaptchaGeneration;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.utils.ManyChecksUtils;
import ru.leymooo.botfilter.utils.ServerPingUtils;

/**
 *
 * @author Leymooo
 */
public class BotFilter
{

    // @Getter
    private static BotFilter instance;

    private static long ONE_MIN = 60000;

    protected final Map<String, Connector> connectedUsersSet = new ConcurrentHashMap<>();
    //UserName, Ip
    protected final Map<String, String> userCache = new ConcurrentHashMap<>();

    @Getter
    private Sql sql;
    @Getter
    private GeoIp geoIp;
    @Getter
    private ServerPingUtils serverPingUtils;

    private int botCounter = 0;
    private long lastAttack = 0, lastCheck = System.currentTimeMillis();
    private CheckState normalState;
    private CheckState attackState;

    public BotFilter(boolean startup)
    {
        instance = this;
        Settings.IMP.reload( new File( "BotFilter", "config.yml" ) );
        checkForUpdates( startup );
        if ( !CachedCaptcha.generated )
        {
            new CaptchaGeneration();
        }
        normalState = getCheckState( Settings.IMP.PROTECTION.NORMAL );
        attackState = getCheckState( Settings.IMP.PROTECTION.ON_ATTACK );
        PacketUtils.init();
        sql = new Sql( this );
        geoIp = new GeoIp( startup );
        serverPingUtils = new ServerPingUtils( this );
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
        geoIp = null;
        sql.close();
        sql = null;
        ManyChecksUtils.clear();
        serverPingUtils.clear();
    }

    /**
     * Сохраняет игрока в памяти и в датебазе
     *
     * @param userName Имя игрока
     * @param address InetAddress игрока
     */
    public void saveUser(String userName, InetAddress address)
    {
        userName = userName.toLowerCase();
        String ip = address.getHostAddress();
        userCache.put( userName, ip );
        if ( sql != null )
        {
            sql.saveUser( userName, ip );
        }
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

    /**
     * Добавляет игрока в мапу
     *
     * @param connector
     */
    public void addConnection(Connector connector)
    {
        connectedUsersSet.put( connector.getName(), connector );
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
     * Проверяет нужно ли игроку проходить проверку
     *
     * @param userName Имя игрока
     * @param address InetAddress игрока
     * @return Нужно ли юзеру проходить проверку
     */
    public boolean needCheck(String userName, InetAddress address)
    {
        String ip = userCache.get( userName.toLowerCase() );
        return ( ip == null || !ip.equals( address.getHostAddress() ) );
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

    /**
     * Проверяет можно ли подключиться с айпи аддресса
     *
     * @param address Айпи аддресс для проверки
     * @param ping Пинг игрока, -1 если нету
     * @return DisconnectReason
     */
    public KickType checkIpAddress(InetAddress address, int ping)
    {
        int mode = isUnderAttack() ? 1 : 0;
        if ( geoIp.isEnabled() && ( Settings.IMP.GEO_IP.MODE == 0 || Settings.IMP.GEO_IP.MODE == mode ) && !geoIp.isAllowed( address ) )
        {
            return KickType.COUNTRY;
        }
        if ( ping != -1 && Settings.IMP.PING_CHECK.MODE != 2 && ( Settings.IMP.PING_CHECK.MODE == 0 || Settings.IMP.PING_CHECK.MODE == mode ) && ping >= Settings.IMP.PING_CHECK.MAX_PING )
        {
            return KickType.PING;
        }
        return null;
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
            URL url = new URL( "http://botfilter.funtime.su/version.txt" );
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout( 1200 );
            try ( BufferedReader in = new BufferedReader(
                    new InputStreamReader( conn.getInputStream() ) ) )
            {
                if ( !in.readLine().trim().equalsIgnoreCase( Settings.IMP.BOT_FILTER_VERSION ) )
                {

                    logger.log( Level.INFO, "§c[BotFilter] §aНайдена новая версия!" );
                    logger.log( Level.INFO, "§c[BotFilter] §aПожалуйста обновитесь!" );
                    if ( startup )
                    {
                        Thread.sleep( 3500l );
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
