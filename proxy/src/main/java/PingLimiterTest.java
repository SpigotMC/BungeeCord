
import ru.leymooo.botfilter.utils.PingLimiter;

public class PingLimiterTest
{

    public static void main(String[] args) throws InterruptedException
    {
        for ( int i = 0; i < 430; i++ )
        {
            PingLimiter.handle();
        }

        System.out.println( PingLimiter.handle() ); //true
        Thread.sleep( 60001 );
        PingLimiter.handle();
        System.out.println( "1: " + PingLimiter.handle() ); //true

        Thread.sleep( 60001 );
        PingLimiter.handle();
        System.out.println( "2: " + PingLimiter.handle() ); //true 

        Thread.sleep( 60001 );
        PingLimiter.handle();
        System.out.println( "3: " + PingLimiter.handle() ); //false

        for ( int i = 0; i < 418; i++ )
        {
            PingLimiter.handle();
        }

        Thread.sleep( 60000 );

        System.out.println( "4: " + PingLimiter.handle() ); //false

        for ( int i = 0; i < 100; i++ )
        {
            PingLimiter.handle();
        }
        System.out.println( PingLimiter.handle() ); // false
    }
}
