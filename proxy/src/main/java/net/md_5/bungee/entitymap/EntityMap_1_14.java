package net.md_5.bungee.entitymap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

class EntityMap_1_14 extends EntityMap
{

    static final EntityMap_1_14 INSTANCE = new EntityMap_1_14();

    EntityMap_1_14()
    {
        addRewrite( 0x00, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Object : PacketPlayOutSpawnEntity
        addRewrite( 0x01, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Experience Orb : PacketPlayOutSpawnEntityExperienceOrb
        addRewrite( 0x03, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Mob : PacketPlayOutSpawnEntityLiving
        addRewrite( 0x04, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Painting : PacketPlayOutSpawnEntityPainting
        addRewrite( 0x05, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Player : PacketPlayOutNamedEntitySpawn
        addRewrite( 0x06, ProtocolConstants.Direction.TO_CLIENT, true ); // Animation : PacketPlayOutAnimation
        addRewrite( 0x08, ProtocolConstants.Direction.TO_CLIENT, true ); // Block Break Animation : PacketPlayOutBlockBreakAnimation
        addRewrite( 0x1B, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Status : PacketPlayOutEntityStatus
        addRewrite( 0x28, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Relative Move : PacketPlayOutRelEntityMove
        addRewrite( 0x29, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Look and Relative Move : PacketPlayOutRelEntityMoveLook
        addRewrite( 0x2A, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Look : PacketPlayOutEntityLook
        addRewrite( 0x2B, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity : PacketPlayOutEntity
        addRewrite( 0x38, ProtocolConstants.Direction.TO_CLIENT, true ); // Remove Entity Effect : PacketPlayOutRemoveEntityEffect
        addRewrite( 0x3B, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Head Look : PacketPlayOutEntityHeadRotation
        addRewrite( 0x3E, ProtocolConstants.Direction.TO_CLIENT, true ); // Camera : PacketPlayOutCamera
        addRewrite( 0x43, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Metadata : PacketPlayOutEntityMetadata
        addRewrite( 0x44, ProtocolConstants.Direction.TO_CLIENT, false ); // Attach Entity : PacketPlayOutAttachEntity
        addRewrite( 0x45, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Velocity : PacketPlayOutEntityVelocity
        addRewrite( 0x46, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Equipment : PacketPlayOutEntityEquipment
        addRewrite( 0x4A, ProtocolConstants.Direction.TO_CLIENT, true ); // Set Passengers : PacketPlayOutMount
        addRewrite( 0x55, ProtocolConstants.Direction.TO_CLIENT, true ); // Collect Item : PacketPlayOutCollect
        addRewrite( 0x56, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Teleport : PacketPlayOutEntityTeleport
        addRewrite( 0x58, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Properties : PacketPlayOutUpdateAttributes
        addRewrite( 0x59, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Effect : PacketPlayOutEntityEffect

        addRewrite( 0x0E, ProtocolConstants.Direction.TO_SERVER, true ); // Use Entity : PacketPlayInUseEntity
        addRewrite( 0x1B, ProtocolConstants.Direction.TO_SERVER, true ); // Entity Action : PacketPlayInEntityAction
    }

    @Override
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    public void rewriteClientbound(ByteBuf packet, int oldId, int newId, int protocolVersion)
    {
        super.rewriteClientbound( packet, oldId, newId );

        // Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
        int jumpIndex = packet.readerIndex();
        switch ( packetId )
        {
            case 0x44 /* Attach Entity : PacketPlayOutAttachEntity */:
                rewriteInt( packet, oldId, newId, readerIndex + packetIdLength + 4 );
                break;
            case 0x55 /* Collect Item : PacketPlayOutCollect */:
                DefinedPacket.skipVarInt( packet );
                rewriteVarInt( packet, oldId, newId, packet.readerIndex() );
                break;
            case 0x4A /* Set Passengers : PacketPlayOutMount */:
                DefinedPacket.skipVarInt( packet );
                jumpIndex = packet.readerIndex();
                // Fall through on purpose to int array of IDs
            case 0x37 /* Destroy Entities : PacketPlayOutEntityDestroy */:
                EntityMap_1_8.rewriteEntityIdArray( packet, oldId, newId, jumpIndex );
                break;
            case 0x00 /* Spawn Object : PacketPlayOutSpawnEntity */:
                rewriteSpawnObject( packet, oldId, newId, 2, 101, 71 );
                break;
            case 0x05 /* Spawn Player : PacketPlayOutNamedEntitySpawn */:
                EntityMap_1_8.rewriteSpawnPlayerUuid( packet, readerIndex, packetIdLength );
                break;
            case 0x32 /* Combat Event : PacketPlayOutCombatEvent */:
                EntityMap_1_8.rewriteCombatEvent( packet, oldId, newId );
                break;
            case 0x43 /* EntityMetadata : PacketPlayOutEntityMetadata */:
                DefinedPacket.skipVarInt( packet ); // Entity ID
                rewriteMetaVarInt( packet, oldId + 1, newId + 1, 7, protocolVersion ); // fishing hook
                rewriteMetaVarInt( packet, oldId, newId, 8, protocolVersion ); // fireworks (et al)
                rewriteMetaVarInt( packet, oldId, newId, 15, protocolVersion ); // guardian beam
                break;
            case 0x50 /* Entity Sound Effect : PacketPlayOutEntitySound */:
                DefinedPacket.skipVarInt( packet );
                DefinedPacket.skipVarInt( packet );
                rewriteVarInt( packet, oldId, newId, packet.readerIndex() );
                break;
        }
        packet.readerIndex( readerIndex );
    }

    @Override
    public void rewriteServerbound(ByteBuf packet, int oldId, int newId)
    {
        super.rewriteServerbound( packet, oldId, newId );

        // Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;

        if ( packetId == 0x2B /* Spectate : PacketPlayInSpectate */ && !BungeeCord.getInstance().getConfig().isIpForward() )
        {
            EntityMap_1_8.rewriteSpectateUuid( packet, readerIndex, packetIdLength );
        }
        packet.readerIndex( readerIndex );
    }

    public static void rewriteSpawnObject(ByteBuf packet, int oldId, int newId, int arrowId, int fishingBobberId, int spectralArrowId)
    {
        DefinedPacket.skipVarInt( packet );
        DefinedPacket.skipUUID( packet );
        int type = DefinedPacket.readVarInt( packet );

        if ( type == arrowId || type == fishingBobberId || type == spectralArrowId ) // arrow, fishing_bobber or spectral_arrow
        {
            if ( type == arrowId || type == spectralArrowId ) // arrow or spectral_arrow
            {
                oldId = oldId + 1;
                newId = newId + 1;
            }

            packet.skipBytes( 26 ); // double, double, double, byte, byte
            int position = packet.readerIndex();
            int readId = packet.readInt();
            if ( readId == oldId )
            {
                packet.setInt( position, newId );
            } else if ( readId == newId )
            {
                packet.setInt( position, oldId );
            }
        }
    }
}
