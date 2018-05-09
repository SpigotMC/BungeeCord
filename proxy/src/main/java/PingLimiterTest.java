
import io.netty.util.ResourceLeakDetector;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.Login;
import ru.leymooo.botfilter.caching.PacketConstans;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.packets.ChunkPacket;
import ru.leymooo.botfilter.packets.PlayerAbilities;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.SetSlot;
import ru.leymooo.botfilter.packets.unused.SpawnPosition;
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
