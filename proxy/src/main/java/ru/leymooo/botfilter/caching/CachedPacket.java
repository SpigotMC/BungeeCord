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

    private ByteBuf[] byteBuf = new ByteBuf[ PacketUtils.PROTOCOLS_NUM ];

    public CachedPacket(DefinedPacket packet, Protocol... protocols)
    {
        PacketUtils.fillArray( byteBuf, packet, protocols );
    }

    public ByteBuf get(int version)
    {
        return byteBuf[PacketUtils.rewriteVersion( version )].retainedDuplicate();
    }

    public void release()
    {
        for ( ByteBuf buf : byteBuf )
        {
            PacketUtils.releaseByteBuf( buf );
        }
        byteBuf = null;
    }
}
