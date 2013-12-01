package net.md_5.bungee;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;

/**
 * Class to rewrite integers within packets.
 */
public class EntityMap
{

    public final static int[][] entityIds = new int[ 256 ][];

    static
    {
        entityIds[0x0A] = new int[]
        {
            0
        };
        entityIds[0x0D] = new int[]
        {
            4
        };
        entityIds[0x12] = new int[]
        {
            0
        };
        entityIds[0x1A] = new int[]
        {
            0
        };
        entityIds[0x1B] = new int[]
        {
            0, 4
        };
        entityIds[0x1C] = new int[]
        {
            0 // TODO: Meta
        };
        entityIds[0x1D] = new int[]
        {
            0
        };
        entityIds[0x1E] = new int[]
        {
            0
        };
        entityIds[0x20] = new int[]
        {
            0
        };
    }

    public static void rewrite(ByteBuf packet, int oldId, int newId)
    {
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
        int[] idArray = entityIds[packetId];

        if ( idArray != null )
        {
            for ( int pos : idArray )
            {
                int readId = packet.getInt( packetIdLength + pos );
                if ( readId == oldId )
                {
                    packet.setInt( packetIdLength + pos, newId );
                }
            }
        }

        if ( packetId == 0x0E )
        {
            DefinedPacket.readVarInt( packet );
            byte type = packet.readByte();
            if ( type == 60 || type == 90 )
            {
                packet.skipBytes( 14 );
                int pos = packet.readerIndex();
                int shooterId = packet.getInt( pos );
                if ( shooterId == oldId )
                {
                    packet.setInt( pos, newId );
                }
            }
        }

        packet.readerIndex( readerIndex );
    }
}
