
import io.netty.util.ResourceLeakDetector;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.Login;
import ru.leymooo.botfilter.caching.PacketsPosition;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.packets.EmptyChunkPacket;
import ru.leymooo.botfilter.packets.PlayerAbilities;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.SetSlot;

public class CachingTest
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
            for ( int a = 0; a < 50000; a++ )
            {
                PacketUtils.getChachedPacket( PacketsPosition.LOGIN ).get( 47 ).release();
                PacketUtils.getChachedPacket( PacketsPosition.CHUNK ).get( 47 ).release();
                PacketUtils.getChachedPacket( PacketsPosition.SETSLOT_MAP ).get( 47 ).release();
                PacketUtils.getChachedPacket( PacketsPosition.PLAYERABILITIES ).get( 47 ).release();
                PacketUtils.getChachedPacket( PacketsPosition.PLAYERPOSANDLOOK_CAPTCHA ).get( 47 ).release();
                PacketUtils.getChachedPacket( PacketsPosition.CHECKING_CAPTCHA ).get( 47 ).release();
                PacketUtils.titles[0].test();
            }
            System.out.println( "From cache: " + ( System.currentTimeMillis() - start ) );
            System.gc();
        }
        String message = ChatColor.translateAlternateColorCodes( '&', Settings.IMP.MESSAGES.CHECKING_CAPTCHA.replace( "%prefix%", Settings.IMP.MESSAGES.PREFIX ).replace( "%nl%", "\n" ) );
        for ( int i = 0; i < 15; i++ )
        {
            long start = System.currentTimeMillis();
            for ( int a = 0; a < 50000; a++ )
            {
                //Пофиг на айди
                PacketUtils.createPacket( new Login( -1, (short) 2, 0, (short) 0, (short) 100, "flat", false ), 1, 47 ).release();
                PacketUtils.createPacket(new EmptyChunkPacket( 0, 0 ), 1, 47 ).release();
                PacketUtils.createPacket( new SetSlot( 0, 36, 358, 1, 0 ), 1, 47 ).release();
                PacketUtils.createPacket( new PlayerAbilities( (byte) 6, 0f, 0f ), 1, 47 ).release();
                PacketUtils.createPacket( new PlayerPositionAndLook( 7.00, 450, 7.00, -5f, 48f, 9876, false ), 1, 47 ).release();
                PacketUtils.createPacket( createMessagePacket( message ), 1, 47 ).release();

            }
            System.out.println( "Create new: " + ( System.currentTimeMillis() - start ) );
            System.gc();
        }

    }

    private static DefinedPacket createMessagePacket(String message)
    {
        return new Chat( ComponentSerializer.toString(
                TextComponent.fromLegacyText(
                        message ) ), (byte) ChatMessageType.CHAT.ordinal() );
    }
}
