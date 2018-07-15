package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.ProtocolConstants;
import ru.leymooo.botfilter.packets.MapDataPacket;

/**
 *
 * @author Leymooo
 */
public class CachedCaptcha
{

    private ByteBuf[] byteBuf18 = new ByteBuf[ 900 ];
    private ByteBuf[] byteBuf19 = new ByteBuf[ 900 ];
    private ByteBuf[] byteBuf113 = new ByteBuf[ 900 ];

    private static final int PACKETID_18 = 52;
    private static final int PACKETID_19 = 36;
    private static final int PACKETID_113 = 38;

    public static boolean generated = false;

    public void createCaptchaPacket(MapDataPacket map, int answer)
    {
        byteBuf18[answer - 100] = PacketUtils.createPacket( map, PACKETID_18, ProtocolConstants.MINECRAFT_1_8 );
        byteBuf19[answer - 100] = PacketUtils.createPacket( map, PACKETID_19, ProtocolConstants.MINECRAFT_1_9 );
        byteBuf113[answer - 100] = PacketUtils.createPacket( map, PACKETID_113, ProtocolConstants.MINECRAFT_1_13 );

    }

    public ByteBuf get(int version, int captcha)
    {
        return version == ProtocolConstants.MINECRAFT_1_8
                ? byteBuf18[captcha - 100].retainedDuplicate()
                : version == ProtocolConstants.MINECRAFT_1_13 
                ? byteBuf113[captcha - 100].retainedDuplicate()
                : byteBuf19[captcha - 100].retainedDuplicate();
    }

    public void release()
    {
        for ( ByteBuf buf : byteBuf18 )
        {
            PacketUtils.releaseByteBuf( buf );
        }
        for ( ByteBuf buf : byteBuf19 )
        {
            PacketUtils.releaseByteBuf( buf );
        }
    }

    //Copy(Only for tests)
    /*
    do not use this
     */
    public ByteBuf getCopy()
    {
        return byteBuf18[800 - 100].copy();
    }
}
