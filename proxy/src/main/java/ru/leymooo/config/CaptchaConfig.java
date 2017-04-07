package ru.leymooo.config;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import ru.leymooo.bungee.connection.StaticMethods;

public class CaptchaConfig {

    private static Set<String> ips = new HashSet<String>();
    public String title;
    public String subtitle;
    public int threads;
    public String wrongCaptcha;
    public String kickBot;
    public String messageTimeout;
    public String messageInvalid;
    public int maxCaptcha;
    public int timeout;
    public int maxConnects;
    public CaptchaConfig() {
        this.loadConfig();
    }

    public static boolean isWhite(String ip) {
        if ( StaticMethods.underAttack) return false;
        if (CaptchaConfig.ips.contains(ip)) {
            StaticMethods.a.incrementAndGet();
            return true;
        }
        return false;
    }

    private void loadConfig() {
        File file = new File("CaptchaConfig.yml");

        try {
            Configuration config;

            if (!file.exists()) {
                file.createNewFile();
                config = new Configuration();
                config.set("Title", "§cВведите эти цифры в чат");
                config.set("SubTitle", "⬇⬇⬇");
                config.set("Wrong-Captcha", "[§cCaptcha§f] Вы ввели неверную капчу");
                config.set("Bot-kick", "[§cCaptcha§f] §cСкорее всего вы бот :(");
                config.set("Image-Generation-Threads", Integer.valueOf(4));
                config.set("Timeout", Integer.valueOf(15000));
                config.set("Message-Timeout", "[§cCaptcha§f] Вы слишком долго вводили капчу");
                config.set("Message-Invalid", "[§cCaptcha§f] Неверная капча, у вас осталось §e%d§f попытк%s");
                config.set("Max-Captchas", Integer.valueOf(1500));
                //
                config.set("Max-connections-per-3-sec", Integer.valueOf(100));
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
                if (!config.contains("Title")) {
                    config.set("Title", "§cВведите эти цифры в чат");
                    config.set("SubTitle", "⬇⬇⬇");
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
                }
                if (!config.contains("Max-connections-per-3-sec")) {
                    config.set("Max-connections-per-3-sec", Integer.valueOf(100));
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
                }
                if (!config.contains("Wrong-Captcha")) {
                    config.set("Wrong-Captcha", "[§cCaptcha§f] Вы ввели неверную капчу");
                    config.set("Bot-kick", "[§cCaptcha§f] §cСкорее всего вы бот :(");
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
                }
            }
            this.title = config.getString("Title");
            this.subtitle = config.getString("SubTitle");
            this.kickBot = config.getString("Bot-kick");
            this.wrongCaptcha = config.getString("Wrong-Captcha");
            this.threads = config.getInt("Image-Generation-Threads",4);
            timeout = config.getInt("Timeout", 15000);
            messageTimeout = config.getString("Message-Timeout");
            messageInvalid = config.getString("Message-Invalid");
            maxCaptcha = config.getInt("Max-Captchas", 1500);
            maxConnects = config.getInt("Max-connections-per-3-sec", 100);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void addIp(String ip) {
        CaptchaConfig.ips.add(ip);
    }
}
