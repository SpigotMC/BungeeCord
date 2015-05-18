package net.md_5.bungee.entitymap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

/**
 * Class to rewrite integers within packets.
 */
public abstract class EntityMap
{

    private final boolean[] clientboundInts = new boolean[ 256 ];
    private final boolean[] clientboundVarInts = new boolean[ 256 ];

    private final boolean[] serverboundInts = new boolean[ 256 ];
    private final boolean[] serverboundVarInts = new boolean[ 256 ];

    EntityMap()
    {
    }

    // Returns the correct entity map for the protocol version
    public static EntityMap getEntityMap(int version)
    {
        switch ( version )
        {
            case ProtocolConstants.MINECRAFT_1_7_2:
                return new EntityMap_1_7_2();
            case ProtocolConstants.MINECRAFT_1_7_6:
                return new EntityMap_1_7_6();
            case ProtocolConstants.MINECRAFT_1_8:
                return new EntityMap_1_8();
        }
        throw new RuntimeException( "Version " + version + " has no entity map" );
    }

    protected void addRewrite(int id, ProtocolConstants.Direction direction, boolean varint)
    {
        if ( direction == ProtocolConstants.Direction.TO_CLIENT )
        {
            if ( varint )
            {
                clientboundVarInts[ id ] = true;
            } else
            {
                clientboundInts[ id ] = true;
            }
        } else
        {
            if ( varint )
            {
                serverboundVarInts[ id ] = true;
            } else
            {
                serverboundInts[ id ] = true;
            }
        }
    }

    public void rewriteServerbound(ByteBuf packet, int oldId, int newId)
    {
        rewrite( packet, oldId, newId, serverboundInts, serverboundVarInts );
    }

    public void rewriteClientbound(ByteBuf packet, int oldId, int newId)
    {
        rewrite( packet, oldId, newId, clientboundInts, clientboundVarInts );
    }

    protected static void rewriteInt(ByteBuf packet, int oldId, int newId, int offset)
    {
        int readId = packet.getInt( offset );
        if ( readId == oldId )
        {
            packet.setInt( offset, newId );
        } else if ( readId == newId )
        {
            packet.setInt( offset, oldId );
        }
    }

    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    protected static void rewriteVarInt(ByteBuf packet, int oldId, int newId, int offset)
    {
        // Need to rewrite the packet because VarInts are variable length
        int readId = DefinedPacket.readVarInt( packet );
        int readIdLength = packet.readerIndex() - offset;
        if ( readId == oldId || readId == newId )
        {
            ByteBuf data = packet.slice().copy();
            packet.readerIndex( offset );
            packet.writerIndex( offset );
            DefinedPacket.writeVarInt( readId == oldId ? newId : oldId, packet );
            packet.writeBytes( data );
            data.release();
        }
    }

    // Handles simple packets
    private static void rewrite(ByteBuf packet, int oldId, int newId, boolean[] ints, boolean[] varints)
    {
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
		
		if(packetId>=0) {
			if ( ints[ packetId ] )
			{
				rewriteInt( packet, oldId, newId, readerIndex + packetIdLength );
			} else if ( varints[ packetId ] )
			{
				rewriteVarInt( packet, oldId, newId, readerIndex + packetIdLength );
			}
		}
        packet.readerIndex( readerIndex );
    }
}
