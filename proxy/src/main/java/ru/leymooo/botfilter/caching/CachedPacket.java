package ru.leymooo.botfilter.caching;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Kick;

/**
 *
 * @author Leymooo
 */
public class CachedPacket
{

    private TIntObjectMap<ByteBuf> byteBuf = new TIntObjectHashMap<>( ProtocolConstants.SUPPORTED_VERSION_IDS.size() );

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
            byteBuf.put( version, PacketUtil.createPacket( packet, packetId, version ) );
            // Создаем для каждой версии свой пакет,
            // по сколько есть пакеты у которых одинаковые айдишки,
            // но для разных версий записывается разная дата 
        }
    }

    public ByteBuf get(int version)
    {
        return byteBuf.get( version ).copy();
    }

    public void release()
    {
        for ( ByteBuf buf : byteBuf.valueCollection() )
        {
            buf.release();
        }
        byteBuf.clear();
    }
}
