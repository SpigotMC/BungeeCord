package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import ru.leymooo.botfilter.Connector;
import ru.leymooo.botfilter.packets.SetExp;

/**
 *
 * @author Leymooo
 */
public class CachedExpPacket
{

    private ByteBuf[][] byteBuf = new ByteBuf[ Connector.TOTAL_TICKS ][ ProtocolConstants.MINECRAFT_1_12_2 + 1 ];

    public CachedExpPacket()
    {
        create();
    }

    private void create()
    {
        int ticks = Connector.TOTAL_TICKS;
        int interval = 2;
        float expinterval = 1f / ( (float) ticks / (float) interval );
        SetExp setExp = new SetExp( 0, 0, 0 );
        for ( int i = 0; i < ticks; i = i + interval )
        {
            setExp.setExpBar( setExp.getExpBar() + expinterval );
            setExp.setLevel( setExp.getLevel() + 1 );
            cache( setExp, i );
        }
    }

    private void cache(DefinedPacket packet, int tick)
    {
        Protocol.DirectionData prot = Protocol.BotFilter.TO_CLIENT;
        int oldPacketId = -1;
        ByteBuf oldBuf = null;
        for ( int version : ProtocolConstants.SUPPORTED_VERSION_IDS )
        {
            int newPacketId;
            newPacketId = prot.getId( packet.getClass(), version );
            if ( newPacketId != oldPacketId )
            {
                oldPacketId = newPacketId;
                oldBuf = PacketUtils.createPacket( packet, oldPacketId, version );
                byteBuf[tick][version] = oldBuf;
            } else
            {
                ByteBuf newBuf = PacketUtils.createPacket( packet, oldPacketId, version );
                if ( newBuf.equals( oldBuf ) )
                {
                    byteBuf[tick][version] = oldBuf;
                    newBuf.release();
                } else
                {
                    oldBuf = newBuf;
                    byteBuf[tick][version] = oldBuf;
                }
            }
        }
    }

    public ByteBuf get(int tick, int version)
    {
        ByteBuf buf = byteBuf[tick][version];
        return buf == null ? null : buf.copy();
    }

    public void release()
    {
        for ( int i = 0; i < byteBuf.length; i++ )
        {
            if ( byteBuf[i] != null )
            {
                for ( ByteBuf buf : byteBuf[i] )
                {
                    if ( buf != null && buf.refCnt() != 0 )
                    {
                        buf.release();
                    }
                }
                byteBuf[i] = null;
            }

        }
        byteBuf = null;
    }
}
