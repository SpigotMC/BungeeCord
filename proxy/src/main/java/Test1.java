
import io.netty.util.ResourceLeakDetector;
import net.md_5.bungee.protocol.packet.KeepAlive;
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
        for ( int i = 0; i < 20; i++ )
        {
            long start = System.currentTimeMillis();
            for ( int a = 0; a < 50000; a++ )
            {
                PacketUtils.singlePackets.get( Login.class ).get( 47 );
                PacketUtils.singlePackets.get( SpawnPosition.class ).get( 47 );
                PacketUtils.singlePackets.get( ChunkPacket.class ).get( 47 );
                PacketUtils.singlePackets.get( SetSlot.class ).get( 47 );
                PacketUtils.singlePackets.get( PlayerAbilities.class ).get( 47 );
                PacketUtils.singlePackets.get( PlayerPositionAndLook.class ).get( 47 );
            }
            System.out.println( "From cache: " + ( System.currentTimeMillis() - start ) );
        }

        for ( int i = 0; i < 20; i++ )
        {
            long start = System.currentTimeMillis();
            for ( int a = 0; a < 50000; a++ )
            {
                //Пофиг на айди
                PacketUtils.createPacket( new Login( -1, (short) 2, 0, (short) 0, (short) 100, "flat", false ), 1, 47 );
                PacketUtils.createPacket( new SpawnPosition( 1, 60, 1 ), 1, 47 );
                PacketUtils.createPacket( new ChunkPacket( 0, 0, new byte[ 63 ], false ), 1, 47 );
                PacketUtils.createPacket( new SetSlot( 0, 36, 358, 1, 0 ), 1, 47 );
                PacketUtils.createPacket( new PlayerAbilities( (byte) 6, 0f, 0f ), 1, 47 );
                PacketUtils.createPacket( new PlayerPositionAndLook( 7.00, 450, 7.00, -5f, 48f, 9876, false ), 1, 47 );
            }
            System.out.println( "Create new: " + ( System.currentTimeMillis() - start ) );
        }

    }

}
