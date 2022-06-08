package ru.leymooo.botfilter.caching;

/**
 * @author Leymooo
 */
public final class PacketsPosition
{

    private PacketsPosition()
    {
    }

    public static int LOGIN = 0;
    public static int CHUNK = 1;
    public static int TIME = 2;
    public static int PLAYERABILITIES = 3;
    public static int PLAYERPOSANDLOOK_CAPTCHA = 4;
    public static int SETSLOT_MAP = 5;
    public static int SETSLOT_RESET = 6;
    public static int KEEPALIVE = 7;
    public static int PLAYERPOSANDLOOK = 8;
    public static int SETEXP_RESET = 9;
    public static int PLUGIN_MESSAGE = 10;



    public static int CAPTCHA_FAILED_2_MSG = 0;
    public static int CAPTCHA_FAILED_1_MSG = 1;
    public static int CHECKING_MSG = 2;
    public static int CHECKING_CAPTCHA_MSG = 3;
    public static int CHECK_SUS_MSG = 4;

}
