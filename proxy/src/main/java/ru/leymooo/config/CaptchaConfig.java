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
    public String wrongCaptcha;
    public String kickBot;
    public String messageTimeout;
    public String messageInvalid;
    public String messageEnter;
    public int timeout;
    public int max;
    public int min;
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
                config.set("Wrong-Captcha", "[§cCaptcha§f] Вы ввели неверную капчу");
                config.set("Bot-kick", "[§cCaptcha§f] §cСкорее всего вы бот :(");
                config.set("Message-Enter", "[§cCaptcha§f] Введите номер с картинки в чат, чтобы пройти проверку. Открыть чат кнопкой \"T\" (английская)");
                config.set("Image-Generation-Threads", Integer.valueOf(4));
                config.set("Timeout", Integer.valueOf(15000));
                config.set("Message-Timeout", "[§cCaptcha§f] Вы слишком долго вводили капчу");
                config.set("Message-Invalid", "[§cCaptcha§f] Неверная капча, у вас осталось §e%d§f попытк%s");
                config.set("Max", Integer.valueOf(999));
                config.set("Min", 100);
                //
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
                if (!config.contains("Wrong-Captcha")) {
                    config.set("Wrong-Captcha", "[§cCaptcha§f] Вы ввели неверную капчу");
                    config.set("Bot-kick", "[§cCaptcha§f] §cСкорее всего вы бот :(");
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
                }
                if (!config.contains("Min")) {
                    config.set("Max", Integer.valueOf(999));
                    config.set("Message-Enter", "[§cCaptcha§f] Введите номер с картинки в чат, чтобы пройти проверку. Открыть чат кнопкой \"T\" (английская)");
                    config.set("Min", 100);
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
                }
            }
            this.kickBot = config.getString("Bot-kick");
            this.wrongCaptcha = config.getString("Wrong-Captcha");
            this.threads = config.getInt("Image-Generation-Threads",4);
            timeout = config.getInt("Timeout", 15000);
            messageTimeout = config.getString("Message-Timeout");
            messageInvalid = config.getString("Message-Invalid");
            max = config.getInt("Max", 999);
            min = config.getInt("Min", 100);
            messageEnter =  config.getString("Message-Enter");
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void addIp(String ip) {
        CaptchaConfig.ips.add(ip);
    }
}
