package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.ProtocolConstants;
import ru.leymooo.botfilter.packets.MapDataPacket;

/**
 * @author Leymooo
 */
public class CachedCaptcha
{

    //уже пора с этим чтото придумать
    private static final int PACKETID_18 = 52;
    private static final int PACKETID_19 = 36;
    private static final int PACKETID_113 = 38;
    private static final int PACKETID_114 = 38;
    private static final int PACKETID_115 = 39;

    private final ByteBuf[] byteBuf18 = new ByteBuf[ 900 ];
    private final ByteBuf[] byteBuf19 = new ByteBuf[ 900 ];
    private final ByteBuf[] byteBuf113 = new ByteBuf[ 900 ];
    private final ByteBuf[] byteBuf114 = new ByteBuf[ 900 ];
    private final ByteBuf[] byteBuf115 = new ByteBuf[ 900 ];

    public static boolean generated = false;

    public void createCaptchaPacket(MapDataPacket map, int answer)
    {
        byteBuf18[answer - 100] = PacketUtils.createPacket( map, PACKETID_18, ProtocolConstants.MINECRAFT_1_8 );
        byteBuf19[answer - 100] = PacketUtils.createPacket( map, PACKETID_19, ProtocolConstants.MINECRAFT_1_9 );
        byteBuf113[answer - 100] = PacketUtils.createPacket( map, PACKETID_113, ProtocolConstants.MINECRAFT_1_13 );
        byteBuf114[answer - 100] = PacketUtils.createPacket( map, PACKETID_114, ProtocolConstants.MINECRAFT_1_14 );
        byteBuf115[answer - 100] = PacketUtils.createPacket( map, PACKETID_115, ProtocolConstants.MINECRAFT_1_15 );

        //TODO: Do something with this shit.
    }

    public ByteBuf get(int version, int captcha)
    {
        if ( version == ProtocolConstants.MINECRAFT_1_8 )
        {
            return byteBuf18[captcha - 100].retainedDuplicate();
        } else if ( version < ProtocolConstants.MINECRAFT_1_13 )
        {
            return byteBuf19[captcha - 100].retainedDuplicate();
        } else if ( version < ProtocolConstants.MINECRAFT_1_14 )
        {
            return byteBuf113[captcha - 100].retainedDuplicate();
        } else if ( version < ProtocolConstants.MINECRAFT_1_15 )
        {
            return byteBuf114[captcha - 100].retainedDuplicate();
        } else
        {
            return byteBuf115[captcha - 100].retainedDuplicate();
        }
    }
}
