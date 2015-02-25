package net.md_5.bungee.entitymap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import java.util.UUID;

class EntityMap_1_8 extends EntityMap
{

    EntityMap_1_8()
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
        addRewrite( 0x49, ProtocolConstants.Direction.TO_CLIENT, true ); // Update Entity NBT

        addRewrite( 0x02, ProtocolConstants.Direction.TO_SERVER, true ); // Use Entity
        addRewrite( 0x0B, ProtocolConstants.Direction.TO_SERVER, true ); // Entity Action
    }

    @Override
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
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
        } else if ( packetId == 0x0C /* Spawn Player */ )
        {
            DefinedPacket.readVarInt( packet ); // Entity ID
            int idLength = packet.readerIndex() - readerIndex - packetIdLength;
            UUID uuid = DefinedPacket.readUUID( packet );
            ProxiedPlayer player;
            if ( ( player = BungeeCord.getInstance().getPlayerByOfflineUUID( uuid ) ) != null )
            {
                int previous = packet.writerIndex();
                packet.readerIndex( readerIndex );
                packet.writerIndex( readerIndex + packetIdLength + idLength );
                DefinedPacket.writeUUID( player.getUniqueId(), packet );
                packet.writerIndex( previous );
            }
        } else if ( packetId == 0x42 /* Combat Event */ )
        {
            int event = packet.readUnsignedByte();
            if ( event == 1 /* End Combat*/ )
            {
                DefinedPacket.readVarInt( packet );
                rewriteInt( packet, oldId, newId, packet.readerIndex() );
            } else if ( event == 2 /* Entity Dead */ )
            {
                int position = packet.readerIndex();
                rewriteVarInt( packet, oldId, newId, packet.readerIndex() );
                packet.readerIndex( position );
                DefinedPacket.readVarInt( packet );
                rewriteInt( packet, oldId, newId, packet.readerIndex() );
            }
        }
        packet.readerIndex( readerIndex );
    }

    @Override
    public void rewriteServerbound(ByteBuf packet, int oldId, int newId)
    {
        super.rewriteServerbound( packet, oldId, newId );
        //Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;

        if ( packetId == 0x18 /* Spectate */ && !BungeeCord.getInstance().getConfig().isIpForward())
        {
            UUID uuid = DefinedPacket.readUUID( packet );
            ProxiedPlayer player;
            if ( ( player = BungeeCord.getInstance().getPlayer( uuid ) ) != null )
            {
                int previous = packet.writerIndex();
                packet.readerIndex( readerIndex );
                packet.writerIndex( readerIndex + packetIdLength );
                DefinedPacket.writeUUID( ( (UserConnection) player ).getPendingConnection().getOfflineId(), packet );
                packet.writerIndex( previous );
            }
        }
        packet.readerIndex( readerIndex );
    }
}
