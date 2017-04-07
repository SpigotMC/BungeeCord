package ru.leymooo.bungee.connection;

import io.netty.buffer.ByteBuf;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.packet.extra.MapDataPacket;
import nl.captcha.Captcha;
import nl.captcha.gimpy.FishEyeGimpyRenderer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.text.producer.NumbersAnswerProducer;

import org.bukkit.map.CraftMapCanvas;

import ru.leymooo.ycore.Connection;

public class CaptchaGenerator {

    private AtomicInteger count = new AtomicInteger();

    public void generate(final int threads, final int max) throws Exception {
        BungeeCord.getInstance().getLogger().info("§cГенерирую капчу(1/2)");
        long start = System.currentTimeMillis();
        for (int in = 0; in < max; in++) {
            //cache for captcha answers
            StaticMethods.strings.add(String.valueOf(in));
            StaticMethods.strings_attack.add(String.valueOf(in));
        }
        int all =  StaticMethods.strings.size();
        count.set(0);
        Connection.maps1_8 = new ByteBuf[all];
        Connection.maps1_9 = new ByteBuf[all];
        Connection.maps1_8_attack = new ByteBuf[all];
        Connection.maps1_9_attack = new ByteBuf[all];
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        int i;
        for (i = 0; i < all;i++) {
            final int i2 = Integer.valueOf(i);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    generateMap(i2, false);
                }
            });
        }

        while ((i = count.get()) != all) {
            System.out.println(i + " из " + all + " [" + (int) ((double) i / (double) all * 100.0D) + " %]");
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        BungeeCord.getInstance().getLogger().info("§cГенерирую капчу(2/2)");
        count.set(0);
        for (i = 0; i < all;i++) {
            final int i2 = Integer.valueOf(i);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    generateMap(i2, true);
                }
            });
        }
        while ((i = count.get()) != all) {
            System.out.println(i + " из " + all + " [" + (int) ((double) i / (double) all * 100.0D) + " %]");
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        System.out.println("Капча сгенерирована за (" + (System.currentTimeMillis() - start) + " мс)");
        start = System.currentTimeMillis();
        System.gc();
        System.out.println("Память очищена за (" + (System.currentTimeMillis() - start) + " мс)");
    }


    public void generateMap(int i, boolean attack) {
        this.count.incrementAndGet();
        CraftMapCanvas map = new CraftMapCanvas();
        Captcha cap = new Captcha.Builder(128, 128)
        .addText(new NumbersAnswerProducer())
        .gimp(new FishEyeGimpyRenderer())
        .addNoise(new CurvedLineNoiseProducer(Color.GREEN, 3))
        .addNoise(new CurvedLineNoiseProducer(Color.GREEN, 3))
        .addNoise(new CurvedLineNoiseProducer(Color.GREEN, 3))
        .build();
        map.drawImage(0, 0,cap.getImage());
        if (attack) { 
            StaticMethods.strings_attack.set(i, cap.getAnswer());
        } else {
            StaticMethods.strings.set(i, cap.getAnswer());
        } 
        MapDataPacket ex = new MapDataPacket(0, (byte) 0, MapDataPacket.Type.IMAGE, map.getMapData());
        try {
            if (!attack) {
                Connection.maps1_8[i] = Connection.getBytes(ex, 52, 47);
                Connection.maps1_9[i] = Connection.getBytes(ex, 36, 107);
            } else {
                Connection.maps1_8_attack[i] = Connection.getBytes(ex, 52, 47);
                Connection.maps1_9_attack[i] = Connection.getBytes(ex, 36, 107);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Ошибка генерации картинок, сообщите разработчику - vk.com/leymooo_s");
            System.exit(0);
        }

    }

}
