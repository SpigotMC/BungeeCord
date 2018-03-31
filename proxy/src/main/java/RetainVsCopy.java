
import io.netty.util.ResourceLeakDetector;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.captcha.CaptchaGeneration;

public class RetainVsCopy
{

    //do not forget to comment loggers in CaptchaGeneration
    public static void main(String[] args) throws Exception
    {
        if ( System.getProperty( "io.netty.leakDetectionLevel" ) == null )
        {
            ResourceLeakDetector.setLevel( ResourceLeakDetector.Level.DISABLED ); // Eats performance
        }
        new CaptchaGeneration();
        for ( int a = 0; a < 15; a++ )
        {
            System.gc();
            long start = System.currentTimeMillis();
            for ( int i = 0; i < 100000; i++ )
            {
                PacketUtils.captchas.get( 47, 800 ).release();
            }
            System.out.println( "Retain: " + ( System.currentTimeMillis() - start ) );
            System.gc();
            start = System.currentTimeMillis();
            for ( int i = 0; i < 100000; i++ )
            {
                PacketUtils.captchas.getCopy().release();
            }
            System.out.println( "Copy: " + ( System.currentTimeMillis() - start ) );
        }
    }

}
