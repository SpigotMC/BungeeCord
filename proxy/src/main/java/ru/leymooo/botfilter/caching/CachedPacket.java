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

    public CachedPacket(DefinedPacket packet, Protocol protocol, Protocol secondProtocol)
    {
        cache( packet, protocol, secondProtocol );
    }

    public CachedPacket(DefinedPacket packet, Protocol protocol)
    {
        cache( packet, protocol, null );
    }

    private void cache(DefinedPacket packet, Protocol protocol, Protocol secondProtocol)
    {
        Protocol.DirectionData prot = protocol.TO_CLIENT;
        Protocol.DirectionData prot2 = secondProtocol == null ? null : secondProtocol.TO_CLIENT;
        int oldPacketId = -1;
        ByteBuf oldBuf = null;
        for ( int version : ProtocolConstants.SUPPORTED_VERSION_IDS )
        {
            int newPacketId;
            try
            {
                newPacketId = prot.getId( packet.getClass(), version );
            } catch ( Exception e )
            {
                newPacketId = prot2.getId( packet.getClass(), version );
            }
            if ( newPacketId != oldPacketId )
            {
                oldPacketId = newPacketId;
                oldBuf = PacketUtils.createPacket( packet, oldPacketId, version );
                byteBuf[version] = oldBuf;
            } else
            {
                ByteBuf newBuf = PacketUtils.createPacket( packet, oldPacketId, version );
                if ( newBuf.equals( oldBuf ) )
                {
                    byteBuf[version] = oldBuf;
                    newBuf.release();
                } else
                {
                    oldBuf = newBuf;
                    byteBuf[version] = oldBuf;
                }
            }
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
            if ( buf != null && buf.refCnt() != 0 )
            {
                buf.release();
            }
        }
        byteBuf = null;
    }
}
