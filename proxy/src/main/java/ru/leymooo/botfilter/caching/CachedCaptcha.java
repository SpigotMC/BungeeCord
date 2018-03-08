package ru.leymooo.botfilter.caching;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import ru.leymooo.botfilter.packets.MapDataPacket;

/**
 *
 * @author Leymooo
 */
public class CachedCaptcha
{

    private TIntObjectMap<ByteBuf> byteBuf18 = new TIntObjectHashMap<>( 889 );
    private TIntObjectMap<ByteBuf> byteBuf19 = new TIntObjectHashMap<>( 899 );

    private int PACKETID_18 = 52;
    private int PACKETID_19 = 36;

    public static boolean generated = false;

    public void createCaptchaPacket(MapDataPacket map, int answer)
    {

        byteBuf18.put( answer, PacketUtils.createPacket( map, PACKETID_18, ProtocolConstants.MINECRAFT_1_8 ) );
        byteBuf19.put( answer, PacketUtils.createPacket( map, PACKETID_19, ProtocolConstants.MINECRAFT_1_9 ) );
    }

    public ByteBuf get(int version, int captcha)
    {
        return version == ProtocolConstants.MINECRAFT_1_8 ? byteBuf18.get( captcha ).copy() : byteBuf19.get( captcha ).copy();
    }

    public void release()
    {
        for ( ByteBuf buf : byteBuf18.valueCollection() )
        {
            buf.release();
        }
        byteBuf18.clear();
        for ( ByteBuf buf : byteBuf19.valueCollection() )
        {
            buf.release();
        }
        byteBuf19.clear();
    }
}
