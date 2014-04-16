package net.md_5.bungee.entitymap;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

class EntityMap_14_11_a extends EntityMap
{
    EntityMap_14_11_a()
    {
        addRewrite( 0x04, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Equipment
        addRewrite( 0x0A, ProtocolConstants.Direction.TO_CLIENT, true ); // Use bed
        addRewrite( 0x0B, ProtocolConstants.Direction.TO_CLIENT, true ); // Animation
        addRewrite( 0x0C, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Player
        addRewrite( 0x0D, ProtocolConstants.Direction.TO_CLIENT, true ); // Collect Item
        addRewrite( 0x0E, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Object
        addRewrite( 0x0F, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Mob
        addRewrite( 0x10, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Painting
        addRewrite( 0x11, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Experience Orb
        addRewrite( 0x12, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Velocity
        addRewrite( 0x14, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity
        addRewrite( 0x15, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Relative Move
        addRewrite( 0x16, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Look
        addRewrite( 0x17, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Look and Relative Move
        addRewrite( 0x18, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Teleport
        addRewrite( 0x19, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Head Look
        addRewrite( 0x1A, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Status
        addRewrite( 0x1B, ProtocolConstants.Direction.TO_CLIENT, false ); // Attach Entity
        addRewrite( 0x1C, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Metadata
        addRewrite( 0x1D, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Effect
        addRewrite( 0x1E, ProtocolConstants.Direction.TO_CLIENT, true ); // Remove Entity Effect
        addRewrite( 0x20, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Properties
        addRewrite( 0x25, ProtocolConstants.Direction.TO_CLIENT, true ); // Block Break Animation
        addRewrite( 0x2C, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Global Entity
        addRewrite( 0x43, ProtocolConstants.Direction.TO_CLIENT, true ); // Camera

        addRewrite( 0x02, ProtocolConstants.Direction.TO_SERVER, true ); // Use Entity
        addRewrite( 0x0B, ProtocolConstants.Direction.TO_SERVER, true ); // Entity Action
    }

    @Override
    public void rewriteClientbound(ByteBuf packet, int oldId, int newId)
    {
        super.rewriteClientbound( packet, oldId, newId );

        //Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
        if ( packetId == 0x0D /* Collect Item */ )
        {
            DefinedPacket.readVarInt( packet );
            rewriteVarInt( packet, oldId, newId, packet.readerIndex() );
        } else if ( packetId == 0x1B /* Attach Entity */ )
        {
            rewriteInt( packet, oldId, newId, readerIndex + packetIdLength + 4 );
        } else if ( packetId == 0x13 /* Destroy Entities */ )
        {
            int count = DefinedPacket.readVarInt( packet );
            int[] ids = new int[ count ];
            for ( int i = 0; i < count; i++ )
            {
                ids[ i ] = DefinedPacket.readVarInt( packet );
            }
            packet.readerIndex( readerIndex + packetIdLength );
            packet.writerIndex( readerIndex + packetIdLength );
            DefinedPacket.writeVarInt( count, packet );
            for ( int id : ids )
            {
                if ( id == oldId )
                {
                    id = newId;
                } else if ( id == newId )
                {
                    id = oldId;
                }
                DefinedPacket.writeVarInt( id, packet );
            }
        } else if ( packetId == 0x0E /* Spawn Object */ )
        {
            DefinedPacket.readVarInt( packet );
            int idLength = packet.readerIndex() - readerIndex - packetIdLength;

            int type = packet.getByte( readerIndex + packetIdLength + idLength );

            if ( type == 60 || type == 90 )
            {
                int readId = packet.getInt( packetIdLength + idLength + 15 );
                int changedId = -1;
                if ( readId == oldId )
                {
                    packet.setInt( packetIdLength + idLength + 15, newId );
                    changedId = newId;
                } else if ( readId == newId )
                {
                    packet.setInt( packetIdLength + idLength + 15, oldId );
                    changedId = newId;
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
        } else if ( packetId == 0x0C /* Spawn Player */ )
        {
            DefinedPacket.readVarInt( packet );
            int idLength = packet.readerIndex() - readerIndex - packetIdLength;
            String uuid = DefinedPacket.readString( packet );
            String username = DefinedPacket.readString( packet );
            int props = DefinedPacket.readVarInt( packet );
            if ( props == 0 )
            {
                UserConnection player = (UserConnection) BungeeCord.getInstance().getPlayer( username );
                if ( player != null )
                {
                    LoginResult profile = player.getPendingConnection().getLoginProfile();
                    if ( profile != null && profile.getProperties() != null
                            && profile.getProperties().length >= 1 )
                    {
                        ByteBuf rest = packet.slice().copy();
                        packet.readerIndex( readerIndex );
                        packet.writerIndex( readerIndex + packetIdLength + idLength );
                        DefinedPacket.writeString( player.getUniqueId().toString(), packet );
                        DefinedPacket.writeString( username, packet );
                        DefinedPacket.writeVarInt( profile.getProperties().length, packet );
                        for ( LoginResult.Property property : profile.getProperties() )
                        {
                            DefinedPacket.writeString( property.getName(), packet );
                            DefinedPacket.writeString( property.getValue(), packet );
                            DefinedPacket.writeString( property.getSignature(), packet );
                        }
                        packet.writeBytes( rest );
                        rest.release();
                    }
                }
            }
        }
        packet.readerIndex( readerIndex );
    }
}
