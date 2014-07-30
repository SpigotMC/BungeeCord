package net.md_5.bungee.entitymap;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

class EntityMap_1_7_2 extends EntityMap
{

    EntityMap_1_7_2()
    {
        addRewrite( 0x04, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Equipment
        addRewrite( 0x0A, ProtocolConstants.Direction.TO_CLIENT, false ); // Use bed
        addRewrite( 0x0B, ProtocolConstants.Direction.TO_CLIENT, true ); // Animation
        addRewrite( 0x0C, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Player
        addRewrite( 0x0D, ProtocolConstants.Direction.TO_CLIENT, false ); // Collect Item
        addRewrite( 0x0E, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Object
        addRewrite( 0x0F, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Mob
        addRewrite( 0x10, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Painting
        addRewrite( 0x11, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Experience Orb
        addRewrite( 0x12, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Velocity
        addRewrite( 0x14, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity
        addRewrite( 0x15, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Relative Move
        addRewrite( 0x16, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Look
        addRewrite( 0x17, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Look and Relative Move
        addRewrite( 0x18, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Teleport
        addRewrite( 0x19, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Head Look
        addRewrite( 0x1A, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Status
        addRewrite( 0x1B, ProtocolConstants.Direction.TO_CLIENT, false ); // Attach Entity
        addRewrite( 0x1C, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Metadata
        addRewrite( 0x1D, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Effect
        addRewrite( 0x1E, ProtocolConstants.Direction.TO_CLIENT, false ); // Remove Entity Effect
        addRewrite( 0x20, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Properties
        addRewrite( 0x25, ProtocolConstants.Direction.TO_CLIENT, true ); // Block Break Animation
        addRewrite( 0x2C, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Global Entity

        addRewrite( 0x02, ProtocolConstants.Direction.TO_SERVER, false ); // Use Entity
        addRewrite( 0x0A, ProtocolConstants.Direction.TO_SERVER, false ); // Animation
        addRewrite( 0x0B, ProtocolConstants.Direction.TO_SERVER, false ); // Entity Action
    }

    @Override
    public void rewriteClientbound(ByteBuf packet, int oldId, int newId)
    {
        super.rewriteClientbound( packet, oldId, newId );

        //Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
        if ( packetId == 0x0D /* Collect Item */ || packetId == 0x1B /* Attach Entity */ )
        {
            rewriteInt( packet, oldId, newId, readerIndex + packetIdLength + 4 );
        } else if ( packetId == 0x13 /* Destroy Entities */ )
        {
            int count = packet.getByte( packetIdLength );
            for ( int i = 0; i < count; i++ )
            {
                rewriteInt( packet, oldId, newId, packetIdLength + 1 + i * 4 );
            }
        } else if ( packetId == 0x0E /* Spawn Object */ )
        {
            DefinedPacket.readVarInt( packet );
            int type = packet.readUnsignedByte();

            if ( type == 60 || type == 90 )
            {
                packet.skipBytes( 14 );
                int position = packet.readerIndex();
                int readId = packet.readInt();
                int changedId = -1;
                if ( readId == oldId )
                {
                    packet.setInt( position, newId );
                    changedId = newId;
                } else if ( readId == newId )
                {
                    packet.setInt( position, oldId );
                    changedId = oldId;
                }
                if ( changedId != -1 )
                {
                    if ( changedId == 0 && readId != 0 )
                    { // Trim off the extra data
                        packet.readerIndex( readerIndex );
                        packet.writerIndex( packet.readableBytes() - 6 );
                    } else if ( changedId != 0 && readId == 0 )
                    { // Add on the extra data
                        packet.readerIndex( readerIndex );
                        packet.capacity( packet.readableBytes() + 6 );
                        packet.writerIndex( packet.readableBytes() + 6 );
                    }
                }
            }
        }
        packet.readerIndex( readerIndex );
    }
}
