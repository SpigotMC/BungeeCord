
import io.netty.util.ResourceLeakDetector;
import net.md_5.bungee.protocol.packet.Login;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.packets.ChunkPacket;
import ru.leymooo.botfilter.packets.PlayerAbilities;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.SetSlot;
import ru.leymooo.botfilter.packets.SpawnPosition;

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
        for ( int i = 0; i < 15; i++ )
        {
            long start = System.currentTimeMillis();
            for ( int a = 0; a < 20000; a++ )
            {
                PacketUtils.packets[0].get( 47 ).release();
                PacketUtils.packets[1].get( 47 ).release();
                PacketUtils.packets[3].get( 47 ).release();
                PacketUtils.packets[6].get( 47 ).release();
                PacketUtils.packets[4].get( 47 ).release();
                PacketUtils.packets[5].get( 47 ).release();
            }
            System.out.println( "From cache: " + ( System.currentTimeMillis() - start ) );
        }

        for ( int i = 0; i < 15; i++ )
        {
            long start = System.currentTimeMillis();
            for ( int a = 0; a < 20000; a++ )
            {
                //Пофиг на айди
                PacketUtils.createPacket( new Login( -1, (short) 2, 0, (short) 0, (short) 100, "flat", false ), 1, 47 ).release();
                PacketUtils.createPacket( new SpawnPosition( 1, 60, 1 ), 1, 47 ).release();
                PacketUtils.createPacket( new ChunkPacket( 0, 0, new byte[ 63 ], false ), 1, 47 ).release();
                PacketUtils.createPacket( new SetSlot( 0, 36, 358, 1, 0 ), 1, 47 ).release();
                PacketUtils.createPacket( new PlayerAbilities( (byte) 6, 0f, 0f ), 1, 47 ).release();
                PacketUtils.createPacket( new PlayerPositionAndLook( 7.00, 450, 7.00, -5f, 48f, 9876, false ), 1, 47 ).release();
            }
            System.out.println( "Create new: " + ( System.currentTimeMillis() - start ) );
        }

    }

}
