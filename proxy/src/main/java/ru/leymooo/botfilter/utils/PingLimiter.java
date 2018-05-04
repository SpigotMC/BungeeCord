package ru.leymooo.botfilter.utils;

/**
 *
 * @author Leymooo
 */
public class PingLimiter
{

    private PingLimiter()
    {

    }

    private static final long ONE_MIN = 60000;
    private static final long BAN_TIME = ONE_MIN * 3;
    private static int THRESHOLD = 300;
    private static long LASTCHECK = System.currentTimeMillis();
    private static int currNumOfPings = 0;
    private static boolean banned = false;

    public static void handle()
    {
        currNumOfPings++;
        long currTime = System.currentTimeMillis();
        if (banned) {
            if (currTime - LASTCHECK > BAN_TIME) {
                banned = false;
                currNumOfPings = 0;
                LASTCHECK = currTime;
            }
            return;
        }
        
        if (currTime - LASTCHECK <= ONE_MIN && currNumOfPings >= THRESHOLD) {
            banned = true;
            LASTCHECK = currTime;
        } else if (currTime - LASTCHECK >= ONE_MIN) {
            currNumOfPings = 0;
            LASTCHECK = currTime;
        }
    }

    public static boolean isBanned()
    {
        return banned;
    }

}
