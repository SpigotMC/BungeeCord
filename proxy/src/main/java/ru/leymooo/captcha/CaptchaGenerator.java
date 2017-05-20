package ru.leymooo.captcha;

import com.github.cage.GCage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.extra.MapDataPacket;
import nl.captcha.Captcha;
import nl.captcha.gimpy.FishEyeGimpyRenderer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.text.producer.NumbersAnswerProducer;
import org.bukkit.map.CraftMapCanvas;
import org.bukkit.map.MapPalette;

/**
 *
 * @author Leymooo
 */
public class CaptchaGenerator
{

    //map cache
    private Random rnd = new Random();
    private ByteBuf[] maps1_8;
    private ByteBuf[] maps1_9;
    @Getter
    private ArrayList<String> answers = new ArrayList<String>();
    @Getter
    private static CaptchaGenerator instance;
    private ExecutorService executor = Executors.newFixedThreadPool( Configuration.getInstance().getThreads() );
    private AtomicInteger progress = new AtomicInteger();

    public CaptchaGenerator()
    {
        instance = this;
        this.generate();
    }

    private void generate()
    {
        int max = Configuration.getInstance().getMaxCaptchas();
        maps1_8 = new ByteBuf[ max ];
        maps1_9 = new ByteBuf[ max ];
        //На всякий случай добавим костылей.
        for ( int a = 0; a < max; a++ )
        {
            answers.add( String.valueOf( a ) );
        }

        int generatorMode = Configuration.getInstance().getMode();
        int rnd = 0;
        for ( int i = 0; i < max; i++ )
        {
            if ( generatorMode == 0 )
            {
                generateOld( String.valueOf( randomInt( 3 ) ), i );
                continue;
            }
            if ( generatorMode == 1 )
            {
                generateNew( String.valueOf( randomInt( 4 ) ), i );
                continue;
            }
            rnd = this.rnd.nextInt( 2 );
            if ( rnd == 0 )
            {
                generateOld( String.valueOf( randomInt( 3 ) ), i );
            } else
            {
                generateNew( String.valueOf( randomInt( 4 ) ), i );
            }
        }
        while ( progress.get()  != max )
        {
            System.out.println( progress.get() + " из " + max + " [" + (int) ( (double) progress.get() / (double) max * 100.0D ) + " %]" );
            try
            {
                Thread.sleep( 1000l );
            } catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }
    }
    private final GCage localGCage = new GCage();

    private void generateOld(final String answer, final int i)
    {
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                CraftMapCanvas map = new CraftMapCanvas();
                map.drawImage( 0, 0, MapPalette.resizeImage( localGCage.drawImage( answer ) ) );
                answers.set( i, answer );
                saveMap( map, i );
                progress.incrementAndGet();
            }
        } );

    }

    private void generateNew(final String answer, final int i)
    {
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                CraftMapCanvas map = new CraftMapCanvas();
                Captcha cap = new Captcha.Builder( 128, 128 )
                        .addText( new NumbersAnswerProducer( 4, answer ) )
                        .gimp( new FishEyeGimpyRenderer() )
                        .addNoise( new CurvedLineNoiseProducer( Color.GREEN, 5 ) )
                        .addNoise( new CurvedLineNoiseProducer( Color.GREEN, 5 ) )
                        .addNoise( new CurvedLineNoiseProducer( Color.GREEN, 5 ) )
                        .build();
                map.drawImage( 0, 0, cap.getImage() );
                answers.set( i, answer );
                saveMap( map, i );
                progress.incrementAndGet();
            }
        } );
    }

    private int randomInt(int lenght)
    {
        return lenght == 4 ? ( rnd.nextInt( 9998 + 1 - 1000 ) + 1000 ) : ( rnd.nextInt( 999 - 100 ) + 100 );
    }

    private void saveMap(CraftMapCanvas map, int i)
    {
        MapDataPacket ex = new MapDataPacket( 0, (byte) 0, MapDataPacket.Type.IMAGE, map.getMapData() );
        try
        {
            maps1_8[i] = getBytes( ex, 52, 47 );
            maps1_9[i] = getBytes( ex, 36, 107 );
        } catch ( Exception exception )
        {
            exception.printStackTrace();
            System.out.println( "Ошибка генерации картинок, сообщите разработчику - vk.com/leymooo_s" );
            System.exit( 0 );
        }
    }

    private ByteBuf getBytes(DefinedPacket packet, int id, int protocol) throws Exception
    {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        DefinedPacket.writeVarInt( id, buffer );
        packet.write( buffer, ProtocolConstants.Direction.TO_CLIENT, protocol );
        return buffer;
    }

    public Object[] getCaptchaAnswerWithPacket(int protocol)
    {
        int pos = rnd.nextInt( answers.size() );
        Object[] values = new Object[ 2 ];
        values[0] = answers.get( pos );
        values[1] = getCaptchaPacket( protocol, pos );
        return values;
    }

    private ByteBuf getCaptchaPacket(int protocol, int pos)
    {
        return protocol > 47 ? maps1_9[pos] : maps1_8[pos];
    }
}
