package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;

/**
 * @author Leymooo
 */
public class CachedPacket
{

    private ByteBuf[] byteBuf = new ByteBuf[ PacketUtils.PROTOCOLS_COUNT ];

    public CachedPacket(DefinedPacket packet, Protocol... protocols)
    {
        if ( packet != null )
        {
            PacketUtils.fillArray( byteBuf, packet, protocols );
        }
    }

    public ByteBuf get(int version)
    {
        ByteBuf buf = byteBuf[PacketUtils.rewriteVersion( version )];
        return buf == null ? null : buf.retainedDuplicate();
    }

    public void release()
    {
        if ( byteBuf != null )
        {
            for ( ByteBuf buf : byteBuf )
            {
                PacketUtils.releaseByteBuf( buf );
            }
            byteBuf = null;
        }
    }
}
