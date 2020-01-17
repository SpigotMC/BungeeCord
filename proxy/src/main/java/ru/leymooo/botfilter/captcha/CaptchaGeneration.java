package ru.leymooo.botfilter.captcha;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.botfilter.caching.CachedCaptcha;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.captcha.generator.CaptchaPainter;
import ru.leymooo.botfilter.captcha.generator.map.CraftMapCanvas;
import ru.leymooo.botfilter.captcha.generator.map.MapPalette;
import ru.leymooo.botfilter.packets.MapDataPacket;

/**
 * @author Leymooo
 */
public class CaptchaGeneration
{

    private Font[] fonts = new Font[]
    {
        new Font( Font.SANS_SERIF, Font.PLAIN, 128 / 2 ),
        new Font( Font.SERIF, Font.PLAIN, 128 / 2 ),
        new Font( Font.MONOSPACED, Font.BOLD, 128 / 2 )
    };

    private CaptchaPainter painter = new CaptchaPainter();

    private ExecutorService executor = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );

    public CaptchaGeneration()
    {
        for ( int i = 100; i <= 999; i++ )
        {
            final int answer = i;
            executor.execute( () ->
            {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                BufferedImage image = painter.draw( fonts[rnd.nextInt( fonts.length )],
                        MapPalette.colors[rnd.nextInt( MapPalette.colors.length )], String.valueOf( answer ) );
                final CraftMapCanvas map = new CraftMapCanvas();
                map.drawImage( 0, 0, image );
                MapDataPacket packet = new MapDataPacket( 0, (byte) 0, map.getMapData() );
                PacketUtils.captchas.createCaptchaPacket( packet, answer );
            } );
        }

        long start = System.currentTimeMillis();
        ThreadPoolExecutor ex = (ThreadPoolExecutor) executor;
        while ( ex.getActiveCount() != 0 )
        {
            BungeeCord.getInstance().getLogger().log( Level.INFO, "[BotFilter] Генерирую капчу [{0}/900]", 900 - ex.getQueue().size() - ex.getActiveCount() );
            try
            {
                Thread.sleep( 1000L );
            } catch ( InterruptedException ex1 )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "[BotFilter] Немогу сгенерировать капчу. Выключаю банджу", ex1 );
                System.exit( 0 );
                return;
            }
        }
        CachedCaptcha.generated = true;
        executor.shutdownNow();
        System.gc();
        BungeeCord.getInstance().getLogger().log( Level.INFO, "[BotFilter] Капча сгенерированна за {0} мс", System.currentTimeMillis() - start );
    }
}
