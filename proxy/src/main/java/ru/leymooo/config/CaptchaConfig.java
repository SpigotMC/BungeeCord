package ru.leymooo.config;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class CaptchaConfig {

    private static Set<String> ips = new HashSet<String>();
    public int threads;
    public static String messageTimeout;
    public static String messageEnter;
    public static String messageInvalid;
    public static int maxCaptcha;
    public static int timeout;

    public CaptchaConfig() {
        this.loadConfig();
    }

    public static boolean isWhite(String ip) {
        return CaptchaConfig.ips.contains(ip);
    }

    private void loadConfig() {
        File file = new File("CaptchaConfig.yml");

        try {
            Configuration config;

            if (!file.exists()) {
                file.createNewFile();
                config = new Configuration();
                config.set("Image-Generation-Threads", Integer.valueOf(4));
                config.set("Log-Join", Boolean.valueOf(true));
                config.set("Timeout", Integer.valueOf(15000));
                config.set("Message-Timeout", "[§cCaptcha§f] Вы слишком долго вводили капчу");
                config.set("Message-Enter", "[§cCaptcha§f] Введите номер с картинки в чат, чтобы пройти проверку. Открыть чат кнопкой \"T\" (английская)");
                config.set("Message-Invalid", "[§cCaptcha§f] Неверная капча, у вас осталось §e%d§f попытк%s");
                config.set("Max-Captchas", Integer.valueOf(1500));
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
                BungeeCord.getInstance().getLogger().warning("§c[CAPTCHA] §eЯ создал конфиг. Редактируй §a\'CaptchaConfig.yml\'§e!");
                BungeeCord.getInstance().getLogger().warning("§c[CAPTCHA] §bЗапуск через 5 сек'");
                Thread.sleep(5000l);
                file = new File("CaptchaConfig.yml");
                if (!file.exists()) {
                    BungeeCord.getInstance().getLogger().warning("§c[CAPTCHA] §eНе могу создать конфиг");
                    System.exit(0);
                    return;
                }
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            } else {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            }

            this.threads = config.getInt("Image-Generation-Threads");
            CaptchaConfig.timeout = config.getInt("Timeout");
            CaptchaConfig.messageTimeout = config.getString("Message-Timeout");
            CaptchaConfig.messageEnter = config.getString("Message-Enter");
            CaptchaConfig.messageInvalid = config.getString("Message-Invalid");
            CaptchaConfig.maxCaptcha = config.getInt("Max-Captchas", 1500);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void addIp(String ip) {
        CaptchaConfig.ips.add(ip);
    }
}
