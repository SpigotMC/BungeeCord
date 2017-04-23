package ru.leymooo.bungee.connection;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.UserConnection;
import ru.leymooo.config.CaptchaConfig;
import ru.leymooo.ycore.Connection;

public class StaticMethods {
    public static final Random random = new Random();
    public static int teleportId;
    public static Map<UserConnection, CaptchaBridge> connections = new ConcurrentHashMap<UserConnection, CaptchaBridge>();
    public static CaptchaConfig sql;
    public static long start = System.currentTimeMillis();
    public static Thread t;
    public static void init() {
        StaticMethods.sql = new CaptchaConfig();
        teleportId = Connection.getTeleportId();
        try {
            CaptchaGenerator captchagenerator = new CaptchaGenerator();
            captchagenerator.generate(sql.threads, sql.max, sql.min);
        } catch (Exception exception) {
            System.out.println("Exception while generate maps");
            exception.printStackTrace();
            System.exit(0);
        }
    }
    public static int getRandomCaptcha() {
        return StaticMethods.random.nextInt(StaticMethods.sql.max - StaticMethods.sql.min) + StaticMethods.sql.min;
    }
    static {
        (new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (!Thread.interrupted()) {
                        try {
                            Thread.sleep(2500L);
                        } catch (InterruptedException interruptedexception) {
                            interruptedexception.printStackTrace();
                        }

                        long curr = System.currentTimeMillis();
                        Iterator<CaptchaBridge> iterator = StaticMethods.connections.values().iterator();
                        CaptchaBridge b;
                        while (iterator.hasNext()) {
                            b = (CaptchaBridge) iterator.next();
                            if (curr - b.getJoinTime() >= (long) StaticMethods.sql.timeout) {
                                b.getCon().disconnect(StaticMethods.sql.messageTimeout);
                                iterator.remove();
                            } else {
                                b.getCon().sendMessage(StaticMethods.sql.messageEnter);
                                if (curr - b.getJoinTime() >= 4000) {
                                    if (b.settings == false || b.tpconfirm == false || b.mcbrand == false || b.alive == false || b.transaction == false) {
                                        b.getCon().disconnect(StaticMethods.sql.kickBot);
                                        iterator.remove();
                                    } 
                                }
                            }
                        }
                    }
                }
            }
        }, "Captcha TimeoutHandler")).start();
    }
}
