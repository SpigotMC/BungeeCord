package ru.leymooo.bungee.connection;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.packet.extra.MapDataPacket;

import org.bukkit.map.CraftMapCanvas;
import org.bukkit.map.MapPalette;

import ru.leymooo.ycore.Connection;

import com.github.cage.GCage;

public class CaptchaGenerator {

    private AtomicInteger count = new AtomicInteger();
    private final GCage localGCage = new GCage();
    private int min;
    public void generate(final int threads, final int max, int min) throws Exception {
        BungeeCord.getInstance().getLogger().info("§cГенерирую капчу(1/2)");
        long start = System.currentTimeMillis();
        int all = max - min + 1;
        this.min = min;
        count.set(0);
        Connection.maps1_8 = new ByteBuf[all];
        Connection.maps1_9 = new ByteBuf[all];
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        int i;

        for (i = min; i <= max; ++i) {
           final int i2 = Integer.valueOf(i);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    generateMap(i2);
                }
            });
        }

        while ((i = this.count.get()) != all) {
            try {
                System.out.println(i + " из " + all + " [" + (int) ((double) i / (double) all * 100.0D) + " %]");
                Thread.sleep(1000L);
            } catch (InterruptedException interruptedexception) {
                return;
            }
        }
        executor.shutdown();
        System.out.println("Капча сгенерирована за (" + (System.currentTimeMillis() - start) + " мс)");
        start = System.currentTimeMillis();
        System.gc();
        System.out.println("Память очищена за (" + (System.currentTimeMillis() - start) + " мс)");
    }


    public void generateMap(int i) {
        this.count.incrementAndGet();
        CraftMapCanvas map = new CraftMapCanvas();
        map.drawImage(0, 0, MapPalette.resizeImage(localGCage.drawImage(String.valueOf(i))));

        MapDataPacket ex = new MapDataPacket(0, (byte) 0, MapDataPacket.Type.IMAGE, map.getMapData());
        try {
            Connection.maps1_8[i - min] = Connection.getBytes(ex, 52, 47);
            Connection.maps1_9[i - min] = Connection.getBytes(ex, 36, 107);
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Ошибка генерации картинок, сообщите разработчику - vk.com/leymooo_s");
            System.exit(0);
        }

    }

}
