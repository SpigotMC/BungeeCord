package ru.leymooo.bungee.connection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.md_5.bungee.BungeeTitle;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import ru.leymooo.config.CaptchaConfig;
import ru.leymooo.ycore.Connection;

public class StaticMethods {
    public static boolean underAttack = false;
    public static HashSet<String> bannedips = new HashSet<String>();
    public static final Random random = new Random();
    public static int teleportId;
    public static ArrayList<String> strings = new ArrayList<String>();
    public static ArrayList<String> strings_attack = new ArrayList<String>();
    public static Map<UserConnection, CaptchaBridge> connections = new ConcurrentHashMap<UserConnection, CaptchaBridge>();
    public static CaptchaConfig sql;
    public static AtomicInteger a = new AtomicInteger();
    public static long start = System.currentTimeMillis();
    public static Thread t;
    public static BungeeTitle title;
    public static BungeeTitle titleClear;
    public static void init() {
        StaticMethods.sql = new CaptchaConfig();
        title = (BungeeTitle) new BungeeTitle().stay(100).fadeIn(1).fadeOut(1)
                .title((BaseComponent)new TextComponent(sql.title))
                .subTitle((BaseComponent)new TextComponent(ChatColor.RED+sql.subtitle));
        titleClear = (BungeeTitle) new BungeeTitle().clear();
        teleportId = Connection.getTeleportId();
        try {
            CaptchaGenerator captchagenerator = new CaptchaGenerator();
            captchagenerator.generate(sql.threads, sql.maxCaptcha);
        } catch (Exception exception) {
            System.out.println("Exception while generate maps");
            exception.printStackTrace();
            System.exit(0);
        }
    }
    public static void resetAllCaptcha() {
        Iterator<CaptchaBridge> iterator = StaticMethods.connections.values().iterator();
        CaptchaBridge b;
        while (iterator.hasNext()) {
            b = (CaptchaBridge) iterator.next();
            b.setJoinTime();
            b.resetCaptcha();
            b.needReset = false;
        }
    }
    public static int getRandomCaptcha() {
        return StaticMethods.underAttack ? StaticMethods.random.nextInt(StaticMethods.strings_attack.size()):StaticMethods.random.nextInt(StaticMethods.strings.size());
    }
    public static void AttackDetected() {
        (StaticMethods.t = new Thread(new Runnable() {
            public void run() {
                try {
                    underAttack = true;
                    resetAllCaptcha();
                    Thread.sleep(1000*60*5);
                    underAttack = false;
                    bannedips.clear();
                    a.set(0);
                    resetAllCaptcha();
                } catch (InterruptedException interruptedexception) {
                    interruptedexception.printStackTrace();
                }
            }
        })).start();
    }
    static {
        (new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (!Thread.interrupted()) {
                        try {
                            Thread.sleep(3000L);
                        } catch (InterruptedException interruptedexception) {
                            interruptedexception.printStackTrace();
                        }

                        long curr = System.currentTimeMillis();
                        Iterator<CaptchaBridge> iterator = StaticMethods.connections.values().iterator();
                        CaptchaBridge b;
                        while (iterator.hasNext()) {
                            b = (CaptchaBridge) iterator.next();
                            title.send(b.getCon());
                            if (curr - b.getJoinTime() >= (long) StaticMethods.sql.timeout) {
                                b.getCon().disconnect(StaticMethods.sql.messageTimeout);
                                if (StaticMethods.underAttack) StaticMethods.bannedips.add(b.ip);
                                iterator.remove();
                            } else {
                                if (StaticMethods.underAttack && curr - b.getJoinTime() >= 3000) {
                                    if (b.settings == false || b.tpconfirm == false || b.mcbrand == false || b.alive == false || b.transaction == false) {
                                        b.getCon().disconnect(StaticMethods.sql.kickBot);
                                        if (StaticMethods.underAttack) StaticMethods.bannedips.add(b.ip);
                                        iterator.remove();
                                    } else if (b.needReset) {
                                        b.setJoinTime();
                                        b.resetCaptcha();
                                        b.needReset = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, "Captcha TimeoutHandler")).start();
        (new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(3000l);
                        StaticMethods.a.set(0);
                    } catch (InterruptedException interruptedexception) {
                        interruptedexception.printStackTrace();
                    }

                }
            }
        }, "Captcha JoinCounter")).start();
    }
}
