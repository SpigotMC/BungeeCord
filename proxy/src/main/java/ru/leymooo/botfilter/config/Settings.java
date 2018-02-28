package ru.leymooo.botfilter.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.BungeeTitle;

public class Settings extends Config
{

    @Ignore
    public static final Settings IMP = new Settings();

    @Comment(
            {
                "Первые 3 настройки не настраиваются",
                "Все ошибки, баги, предложения и прочее просьба писать на гитхабе, "
            })
    @Final
    public final String ISSUES = "https://github.com/Dimatert9/BungeeCord/issues";
    @Final
    public int CONFIG_VERSION = 1;
    @Final
    public double BOT_FILTER_VERSION = 3.0;

    @Create
    public MESSAGES MESSGAGES;

    public static class MESSAGES
    {

        public String PREFIX = "&b&lBot&d&lFilter ";
        public String CHECKING = "%prefix%>> &aИдёт проверка, ожидайте...";
        public String CHECKING_CAPTCHA = "%prefix%&7>> &aВведите номер с картинки в чат";
        public String SUCCESSFULLY = "%prefix%&7>> &aПроверка пройдена, приятной игры";
        public String KICK_MANY_CHECKS = "%prefix%%nl%%nl%&cС вашего айпи замечена подозрительная активность%nl%%nl%&6Повторите попытку через 10 минут";
        public String KICK_NOT_PLAYER = "%prefix%%nl%%nl%&cВы не прошли проверку, возможно вы бот%nl%&7&oЕсли это не так, пожалуйста повторите попытку";
        public String KICK_PROXY = "%prefix%%nl%%nl%&cОбнаружено использование VPN/Прокси";
        public String KICK_COUNTRY = "%prefix%%nl%%nl%&cВаша страна запрещена на серверве";
        public String KICK_BIG_PING = "%prefix%%nl%%nl%&cУ вас очень высокий пинг, скорее всего вы бот";

    }

    @Comment(
            {
                "Сколько игроков/ботов должно зайти за 1 минуту, чтобы включилась защита",
                "Рекомендуемые параметры когда нет рекламы: ",
                "До 150 онлайна - 25, до 250 - 30, до 350 - 35, до 550 - 40,45, выше - подстраивать под себя ",
                "Во время рекламы или когда токо, токо поставили защиту рекомендуется повышать эти значения"
            })
    public int PROTECTION_THRESHOLD = 30;

    @Comment("Как долго активна автоматическая защита? В миллисекундах. 1 сек = 1000")
    public int PROTECTION_TIME = 120000;

    @Comment("Показывать ли онлайн с фильтра")
    public boolean SHOW_ONLINE = true;

    @Comment("Время суток во вермя проверки. https://minecraft.gamepedia.com/File:Day_Night_Clock_24h.png")
    public int WORLD_TIME = 18000;

    @Comment("Сколько времени есть у игрока чтобы пройти защиту. В миллисекундах. 1 сек = 1000")
    public int TIME_OUT = 12000;

    @Ignore
    public BungeeTitle CHECKING;
    public BungeeTitle SUCCESSFULLY;

    @Create
    public GEO_IP GEO_IP;

    @Comment("Включить или отключить GeoIp")
    public static class GEO_IP
    {

        @Comment(
                {
                    "Когда работает проверка",
                    "0 - Всегда",
                    "1 - Только во время бот атаки",
                    "2 - Отключить(также отключается проверка на прокси)"
                })
        public int MODE = 1;
        @Comment(
                {
                    "Откуда качать GEOIP",
                    "Меняйте ссылку если по какой-то причине не качается по этой",
                    "Файл должен заканчиваться на .mmdb или быть запакован в .tar.gz"
                })
        public String GEOIP_DOWNLOAD_URL = "http://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.tar.gz";
        @Comment("Разрешённые странны")
        public List<String> ALLOWED_COUNTRIES = Arrays.asList( "RU", "UA", "BY", "KZ", "EE", "MD", "KG", "AZ", "LT", "LV", "GE", "PL" );
    }

    @Create
    public PING_CHECK PING_CHECK;

    @Comment("Включить или отключить проверку на выскокий пинг")
    public static class PING_CHECK
    {

        @Comment(
                {
                    "Когда работает проверка",
                    "0 - Всегда",
                    "1 - Только во время бот атаки",
                    "2 - Отключить"
                })
        public int MODE = 1;
        @Comment("Максимальный допустимый пинг")
        public int MAX_PING = 330;
    }

    @Create
    public SERVER_PING_CHECK SERVER_PING_CHECK;

    @Comment("Включить или отключить проверку на прямое подключение")
    public static class SERVER_PING_CHECK
    {

        @Comment(
                {
                    "Когда работает проверка",
                    "0 - Всегда",
                    "1 - Только во время бот атаки",
                    "2 - Отключить"
                })
        public int MODE = 1;
        @Comment("В течении какого времени можно заходить на сервер после получения мотд сервера")
        public int CACHE_TIME = 12;
        public String KICK_MESSAGE = "|-\n"
                + "    &cВы были кикнуты! Не используйте прямое подключение\n"
                + "    \n"
                + "    &bДля того чтобы зайти на сервер:\n"
                + "    \n"
                + "    &71) &rДобавте сервер в &lсписок серверов.\n"
                + "    &lНаш айпи &8>> &b&lIP\n"
                + "    \n"
                + "    &72) &rОбновите список серверов. \n"
                + "    &oЧтобы его обновить нажмите кнопку &c&lОбновить &r&oили &c&lRefresh\n"
                + "    \n"
                + "    &73) &rПодождите &c1-3&r секунды и заходите!";
    }

    @Create
    public PROXY PROXY;

    @Comment("Включить или отключить проверку на прокси")
    public static class PROXY
    {

        @Comment(
                {
                    "Когда работает проверка",
                    "0 - Всегда",
                    "1 - Только во время бот атаки",
                    "2 - Отключить"
                })
        public int MODE = 1;
        @Comment(
                {
                    "Откуда качать прокси",
                    "Указывать ссылки, где прокси идут сплошным текстом"
                })
        public List<String> PROXY_SITES = Arrays.asList( "http://fallback.funtime.su/proxy.txt" );
    }

    @Create
    public PROTECTION PROTECTION;

    @Comment(
            {
                "Настройка как именно будет работать защита",
                "0 - Только проверка на падение",
                "1 - Только проверка с помошью капчи",
                "2 - Проверка на падение + капча",
                "3 - Проверка на падение, если провалилась, то капча"
            })
    public static class PROTECTION
    {

        public int NORMAL = 3;
        public int ON_ATTACK = 2;
        /*
        @Comment(
                {
                    "Когда работают дополнительные проверки по протоколу",
                    "    (Пакеты на которые клиент должен всегда отвечать)",
                    "0 - Всегда",
                    "1 - Только во время бот атаки",
                    "2 - Отключить"
                })
        public int ADDITIONAL_CHECKS = 1;
         */
    }

    @Create
    public SQL SQL;

    @Comment("Настройка датабазы")
    public static class SQL
    {

        @Comment("Тип датабазы. sqlite или mysql")
        public String STORAGE_TYPE = "sqlite";
        @Comment("Через сколько дней удалять игроков из датабазы, которые прошли проверку и больше не заходили")
        public int PURGE_TIME = 14;
        @Comment("Настройки для mysql")
        public String HOSTNAME = "127.0.0.1";
        public int PORT = 3306;
        public String USER = "user";
        public String PASSWORD = "password";
        public String DATABASE = "database";
    }

    public void reload(File file)
    {
        load( file );
        save( file );
    }
}
