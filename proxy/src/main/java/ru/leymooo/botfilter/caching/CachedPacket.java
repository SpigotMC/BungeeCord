package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;

/**
 *
 * @author Leymooo
 */
public class CachedPacket
{

    private ByteBuf[] byteBuf = new ByteBuf[ ProtocolConstants.MINECRAFT_1_12_2 + 1 ];

    public CachedPacket(DefinedPacket packet, Protocol protocol)
    {
        cache( packet, protocol );
    }

    private void cache(DefinedPacket packet, Protocol protocol)
    {
        Protocol.DirectionData prot = protocol.TO_CLIENT;
        for ( int version : ProtocolConstants.SUPPORTED_VERSION_IDS )
        {
            int packetId = prot.getId( packet.getClass(), version );
            byteBuf[version] = PacketUtils.createPacket( packet, packetId, version );
            // Создаем для каждой версии свой пакет,
            // по сколько есть пакеты у которых одинаковые айдишки,
            // но для разных версий записывается разная дата 
        }
    }

    public ByteBuf get(int version)
    {
        return byteBuf[version].copy();
    }

    public void release()
    {
        for ( ByteBuf buf : byteBuf )
        {
            if ( buf != null )
            {
                buf.release();
            }
        }
        byteBuf = null;
    }
}
