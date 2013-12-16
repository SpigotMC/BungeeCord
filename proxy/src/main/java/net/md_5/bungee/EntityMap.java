package net.md_5.bungee;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;

/**
 * Class to rewrite integers within packets.
 */
public class EntityMap
{

    private final static boolean[] clientboundInts = new boolean[256];
    private final static boolean[] clientboundVarInts = new boolean[256];

    private final static boolean[] serverboundInts = new boolean[256];
    private final static boolean[] serverboundVarInts = new boolean[256];

    static
    {
        clientboundInts[0x04] = true;
        clientboundInts[0x0A] = true;
        clientboundVarInts[0x0B] = true;
        clientboundVarInts[0x0C] = true;
        clientboundInts[0x0D] = true;
        clientboundVarInts[0x0E] = true;
        clientboundVarInts[0x0F] = true;
        clientboundVarInts[0x10] = true;
        clientboundVarInts[0x11] = true;
        clientboundInts[0x12] = true;
        clientboundInts[0x14] = true;
        clientboundInts[0x15] = true;
        clientboundInts[0x16] = true;
        clientboundInts[0x17] = true;
        clientboundInts[0x18] = true;
        clientboundInts[0x19] = true;
        clientboundInts[0x1A] = true;
        clientboundInts[0x1B] = true;
        clientboundInts[0x1C] = true;
        clientboundInts[0x1D] = true;
        clientboundInts[0x1E] = true;
        clientboundInts[0x20] = true;
        clientboundVarInts[0x25] = true;
        clientboundVarInts[0x2C] = true;

        serverboundInts[0x02] = true;
        serverboundInts[0x0A] = true;
        serverboundInts[0x0B] = true;
    }

    public static void rewriteServerbound(ByteBuf packet, int serverEntityId, int clientEntityId)
    {
        rewrite(packet, serverEntityId, clientEntityId, serverboundInts, serverboundVarInts);
    }

    public static void rewriteClientbound(ByteBuf packet, int serverEntityId, int clientEntityId)
    {
        rewrite(packet, serverEntityId, clientEntityId, clientboundInts, clientboundVarInts);

        //Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
        if ( packetId == 0x0D || packetId == 0x1B )
        {
            int readId = packet.getInt( packetIdLength + 4 );
            if ( readId == serverEntityId )
            {
                packet.setInt( packetIdLength + 4, clientEntityId );
            } else if ( readId == clientEntityId )
            {
                packet.setInt( packetIdLength + 4, serverEntityId );
            }
        } else if ( packetId == 0x13 )
        {
            int count = packet.getByte( packetIdLength );
            for ( int i = 0; i < count; i++ )
            {
                int readId = packet.getInt( packetIdLength + 1 + i * 4);
                if ( readId == serverEntityId )
                {
                    packet.setInt( packetIdLength + 1 + i * 4, clientEntityId );
                } else if ( readId == clientEntityId )
                {
                    packet.setInt( packetIdLength + 1 + i * 4, serverEntityId );
                }
            }
        } else if ( packetId == 0x0E )
        {
            DefinedPacket.readVarInt( packet );
            int idLength = packet.readerIndex() - readerIndex - packetIdLength;

            int type = packet.getByte( packetIdLength + idLength );

            if ( type == 60 || type == 90 )
            {
                int readId = packet.getInt( packetIdLength + idLength + 15 );
                if ( readId == serverEntityId )
                {
                    packet.setInt( packetIdLength + idLength + 15, clientEntityId );
                } else if ( readId == clientEntityId )
                {
                    packet.setInt( packetIdLength + idLength + 15, serverEntityId );
                }
            }
        }
        packet.readerIndex( readerIndex );
    }

    private static void rewrite(ByteBuf packet, int serverEntityId, int clientEntityId, boolean[] ints, boolean[] varints)
    {
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;

        if ( ints[packetId] )
        {
            int readId = packet.getInt( packetIdLength );
            if ( readId == serverEntityId )
            {
                packet.setInt( packetIdLength, clientEntityId );
            } else if ( readId == clientEntityId )
            {
                packet.setInt( packetIdLength, serverEntityId );
            }
        } else if ( varints[packetId] )
        {
            // Need to rewrite the packet because VarInts are variable length
            int readId = DefinedPacket.readVarInt( packet );
            int readIdLength = packet.readerIndex() - readerIndex - packetIdLength;
            if ( readId == serverEntityId || readId == clientEntityId )
            {
                ByteBuf data = packet.slice().copy();
                packet.readerIndex( readerIndex );
                packet.writerIndex( packetIdLength );
                DefinedPacket.writeVarInt( readId == serverEntityId ? clientEntityId : serverEntityId, packet );
                packet.writeBytes( data );
                data.release();
            }
        }
        packet.readerIndex( readerIndex );
    }
}
