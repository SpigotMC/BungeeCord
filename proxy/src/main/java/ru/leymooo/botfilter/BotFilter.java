package ru.leymooo.botfilter;

import java.io.File;
import java.net.InetAddress;
import javolution.util.FastMap;
import lombok.Getter;
import ru.leymooo.botfilter.utils.GeoIp;
import ru.leymooo.botfilter.utils.Proxy;
import ru.leymooo.botfilter.caching.PacketUtil;
import ru.leymooo.botfilter.caching.PacketUtil.KickType;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.utils.ServerPingUtils;

/**
 *
 * @author Leymooo
 */
public class BotFilter
{

    @Getter
    private static BotFilter instance;
    private static long ONE_MIN = 60000;

    protected final FastMap<String, Connector> connectedUsersSet = new FastMap<>();
    //UserName, Ip
    private final FastMap<String, String> userCache = new FastMap<>();

    private Sql sql;
    @Getter
    private Proxy proxy;
    @Getter
    private GeoIp geoIp;
    @Getter
    private ServerPingUtils serverPingUtils;

    private int botCounter = 0;
    private long lastAttack = 0, lastCheck = System.currentTimeMillis();
    private CheckState normalState = getCheckState( Settings.IMP.PROTECTION.NORMAL );
    private CheckState attackState = getCheckState( Settings.IMP.PROTECTION.ON_ATTACK );

    public BotFilter()
    {
        BotFilterThread.stop();
        instance = this;
        Settings.IMP.reload( new File( "BotFilter", "config.yml" ) );
        PacketUtil.init();
        sql = new Sql();
        geoIp = new GeoIp();
        proxy = new Proxy();
        serverPingUtils = new ServerPingUtils();
        BotFilterThread.start();
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
        if ( proxy.isEnabled() && ( Settings.IMP.PROXY.MODE == 0 || Settings.IMP.PROXY.MODE == mode ) && proxy.isProxy( address ) )
        {
            return KickType.PROXY;
        }
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
                return CheckState.ONLY_POSITION;
            case 1:
                return CheckState.ONLY_CAPTCHA;
            case 2:
                return CheckState.CAPTCHA_POSITION;
            case 3:
                return CheckState.CAPTCHA_ON_POSITION_FAILED;
            default:
                return CheckState.ONLY_POSITION;
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
