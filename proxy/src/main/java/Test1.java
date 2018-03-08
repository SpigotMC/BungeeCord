
import io.netty.util.ResourceLeakDetector;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author michael
 */
public class Test1
{

    public static void main(String[] args)
    {
        if ( System.getProperty( "io.netty.leakDetectionLevel" ) == null )
        {
            ResourceLeakDetector.setLevel( ResourceLeakDetector.Level.DISABLED ); // Eats performance
        }
        try
        {
            PacketUtils.init();
        } catch ( Exception ignore )
        {

        }
        for ( int i = 0; i < 20; i++ )
        {
            long start = System.currentTimeMillis();
            for ( int a = 0; a < 50000; a++ )
            {
                PacketUtils.singlePackets.get( PlayerPositionAndLook.class ).get( 47 );
            }
            System.out.println( "From cache: " + ( System.currentTimeMillis() - start ) );
        }
        
        for ( int i = 0; i < 20; i++ )
        {
            long start = System.currentTimeMillis();
            for ( int a = 0; a < 50000; a++ )
            {
                PacketUtils.createPacket( new PlayerPositionAndLook( 7.00, 450, 7.00, -5f, 48f, 9876, false ), 8, 47 );
            }
            System.out.println( "Create new: " + ( System.currentTimeMillis() - start ) );
        }

    }

}
