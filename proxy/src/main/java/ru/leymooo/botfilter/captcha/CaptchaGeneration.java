package ru.leymooo.botfilter.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import lombok.experimental.UtilityClass;
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
@UtilityClass
public class CaptchaGeneration
{
    public void generateImages()
    {
        Random rnd = new Random();
        ThreadLocal<Font[]> fonts = ThreadLocal.withInitial( () -> new Font[]
        {
            new Font( Font.SANS_SERIF, Font.PLAIN, rnd.nextInt( 5 ) + 62 ),
            new Font( Font.SERIF, Font.PLAIN, rnd.nextInt( 5 ) + 62 ),
            new Font( Font.MONOSPACED, Font.BOLD, rnd.nextInt( 5 ) + 62 )
        } );

        ExecutorService executor = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );
        CaptchaPainter painter = new CaptchaPainter();
        MapPalette.prepareColors();
        for ( int i = 100; i <= 999; i++ )
        {
            final int answer = i;
            executor.execute( () ->
            {
                Font[] curr = fonts.get();
                BufferedImage image = painter.draw( curr[rnd.nextInt( curr.length )], randomNotWhiteColor( rnd ),
                    String.valueOf( answer ) );
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


    private static Color randomNotWhiteColor(Random random)
    {
        Color color = MapPalette.colors[random.nextInt( MapPalette.colors.length )];
        if ( color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255 )
        {
            return randomNotWhiteColor( random );
        }
        return color;
    }
}
