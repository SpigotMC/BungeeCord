package net.md_5.bungee.entitymap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.UUID;

class EntityMap_SNAPSHOT extends EntityMap
{

    EntityMap_SNAPSHOT()
    {
        addRewrite( 0x00, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Object : PacketPlayOutSpawnEntity
        addRewrite( 0x01, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Experience Orb : PacketPlayOutSpawnEntityExperienceOrb
        addRewrite( 0x03, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Mob : PacketPlayOutSpawnEntityLiving
        addRewrite( 0x04, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Painting : PacketPlayOutSpawnEntityPainting
        addRewrite( 0x05, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Player : PacketPlayOutNamedEntitySpawn
        addRewrite( 0x06, ProtocolConstants.Direction.TO_CLIENT, true ); // Animation : PacketPlayOutAnimation
        addRewrite( 0x08, ProtocolConstants.Direction.TO_CLIENT, true ); // Block Break Animation : PacketPlayOutBlockBreakAnimation
        addRewrite( 0x1B, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Status : PacketPlayOutEntityStatus
        addRewrite( 0x26, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Relative Move : PacketPlayOutRelEntityMove
        addRewrite( 0x27, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Look and Relative Move : PacketPlayOutRelEntityMoveLook
        addRewrite( 0x28, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Look : PacketPlayOutEntityLook
        addRewrite( 0x29, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity : PacketPlayOutEntity
        addRewrite( 0x2C, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Global Entity
        addRewrite( 0x30, ProtocolConstants.Direction.TO_CLIENT, true ); // Use bed : PacketPlayOutBed
        addRewrite( 0x32, ProtocolConstants.Direction.TO_CLIENT, true ); // Remove Entity Effect : PacketPlayOutRemoveEntityEffect
        addRewrite( 0x35, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Head Look : PacketPlayOutEntityHeadRotation
        addRewrite( 0x37, ProtocolConstants.Direction.TO_CLIENT, true ); // Camera : PacketPlayOutCamera
        addRewrite( 0x3A, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Metadata : PacketPlayOutEntityMetadata
        addRewrite( 0x3C, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Velocity : PacketPlayOutEntityVelocity
        addRewrite( 0x3D, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Equipment : PacketPlayOutEntityEquipment
        addRewrite( 0x41, ProtocolConstants.Direction.TO_CLIENT, false ); // Attach Entity : PacketPlayOutMount
        addRewrite( 0x4A, ProtocolConstants.Direction.TO_CLIENT, true ); // Collect Item : PacketPlayOutCollect
        addRewrite( 0x4B, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Teleport : PacketPlayOutEntityTeleport
        addRewrite( 0x4C, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Properties : PacketPlayOutUpdateAttributes
        addRewrite( 0x4D, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Effect : PacketPlayOutEntityEffect

        addRewrite( 0x0A, ProtocolConstants.Direction.TO_SERVER, true ); // Use Entity : PacketPlayInUseEntity
        addRewrite( 0x14, ProtocolConstants.Direction.TO_SERVER, true ); // Entity Action : PacketPlayInEntityAction
    }

    @Override
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    public void rewriteClientbound(ByteBuf packet, int oldId, int newId)
    {
        super.rewriteClientbound( packet, oldId, newId );

        // Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
        switch ( packetId )
        {
            case 0x4A /* Collect Item : PacketPlayOutCollect */:
                DefinedPacket.readVarInt( packet );
                rewriteVarInt( packet, oldId, newId, packet.readerIndex() );
                break;
            case 0x41 /* Attach Entity : PacketPlayOutMount */:
                DefinedPacket.readVarInt( packet );
                // Fall through on purpose to int array of IDs
            case 0x31 /* Destroy Entities : PacketPlayOutEntityDestroy */:
                int count = DefinedPacket.readVarInt( packet );
                int[] ids = new int[ count ];
                for ( int i = 0; i < count; i++ )
                {
                    ids[i] = DefinedPacket.readVarInt( packet );
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
                break;
            case 0x00 /* Spawn Object : PacketPlayOutSpawnEntity */:
                if ( true )
                {
                    throw new RuntimeException( "TODO" );
                }
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
                break;
            case 0x0C /* Spawn Player : PacketPlayOutNamedEntitySpawn */:
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
                break;
            case 0x2D /* Combat Event : PacketPlayOutCombatEvent */:
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
                break;
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

        if ( packetId == 0x1B /* Spectate : PacketPlayInSpectate */ && !BungeeCord.getInstance().getConfig().isIpForward() )
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
