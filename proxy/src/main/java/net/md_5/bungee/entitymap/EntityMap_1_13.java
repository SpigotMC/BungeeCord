package net.md_5.bungee.entitymap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.ProtocolConstants;

class EntityMap_1_13 extends EntityMap
{

    static final EntityMap_1_13 INSTANCE = new EntityMap_1_13();

    EntityMap_1_13()
    {
        addRewrite( 0x00, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Object : PacketPlayOutSpawnEntity
        addRewrite( 0x01, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Experience Orb : PacketPlayOutSpawnEntityExperienceOrb
        addRewrite( 0x03, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Mob : PacketPlayOutSpawnEntityLiving
        addRewrite( 0x04, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Painting : PacketPlayOutSpawnEntityPainting
        addRewrite( 0x05, ProtocolConstants.Direction.TO_CLIENT, true ); // Spawn Player : PacketPlayOutNamedEntitySpawn
        addRewrite( 0x06, ProtocolConstants.Direction.TO_CLIENT, true ); // Animation : PacketPlayOutAnimation
        addRewrite( 0x08, ProtocolConstants.Direction.TO_CLIENT, true ); // Block Break Animation : PacketPlayOutBlockBreakAnimation
        addRewrite( 0x1C, ProtocolConstants.Direction.TO_CLIENT, false ); // Entity Status : PacketPlayOutEntityStatus
        addRewrite( 0x27, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity : PacketPlayOutEntity
        addRewrite( 0x28, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Relative Move : PacketPlayOutRelEntityMove
        addRewrite( 0x29, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Look and Relative Move : PacketPlayOutRelEntityMoveLook
        addRewrite( 0x2A, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Look : PacketPlayOutEntityLook
        addRewrite( 0x33, ProtocolConstants.Direction.TO_CLIENT, true ); // Use bed : PacketPlayOutBed
        addRewrite( 0x36, ProtocolConstants.Direction.TO_CLIENT, true ); // Remove Entity Effect : PacketPlayOutRemoveEntityEffect
        addRewrite( 0x39, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Head Look : PacketPlayOutEntityHeadRotation
        addRewrite( 0x3C, ProtocolConstants.Direction.TO_CLIENT, true ); // Camera : PacketPlayOutCamera
        addRewrite( 0x3F, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Metadata : PacketPlayOutEntityMetadata
        addRewrite( 0x40, ProtocolConstants.Direction.TO_CLIENT, false ); // Attach Entity : PacketPlayOutAttachEntity
        addRewrite( 0x41, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Velocity : PacketPlayOutEntityVelocity
        addRewrite( 0x42, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Equipment : PacketPlayOutEntityEquipment
        addRewrite( 0x46, ProtocolConstants.Direction.TO_CLIENT, true ); // Set Passengers : PacketPlayOutMount
        addRewrite( 0x4F, ProtocolConstants.Direction.TO_CLIENT, true ); // Collect Item : PacketPlayOutCollect
        addRewrite( 0x50, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Teleport : PacketPlayOutEntityTeleport
        addRewrite( 0x52, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Properties : PacketPlayOutUpdateAttributes
        addRewrite( 0x53, ProtocolConstants.Direction.TO_CLIENT, true ); // Entity Effect : PacketPlayOutEntityEffect

        addRewrite( 0x0D, ProtocolConstants.Direction.TO_SERVER, true ); // Use Entity : PacketPlayInUseEntity
        addRewrite( 0x19, ProtocolConstants.Direction.TO_SERVER, true ); // Entity Action : PacketPlayInEntityAction
    }

    @Override
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    public void rewriteClientbound(PacketWrapper wrapper, int oldId, int newId, int protocolVersion)
    {
        super.rewriteClientbound( wrapper, oldId, newId );
        ByteBuf packet = wrapper.buf;

        // Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int readerIndexAfterPID = packet.readerIndex();
        switch ( packetId )
        {
            case 0x40 /* Attach Entity : PacketPlayOutAttachEntity */:
                rewriteInt( wrapper, oldId, newId, readerIndexAfterPID + 4 );
                break;
            case 0x4F /* Collect Item : PacketPlayOutCollect */:
                DefinedPacket.skipVarInt( packet );
                rewriteVarInt( wrapper, oldId, newId, packet.readerIndex() );
                break;
            case 0x46 /* Set Passengers : PacketPlayOutMount */:
                DefinedPacket.skipVarInt( packet );
                EntityMap_1_8.rewriteEntityIdArray( wrapper, oldId, newId, packet.readerIndex() );
                break;
            case 0x35 /* Destroy Entities : PacketPlayOutEntityDestroy */:
                EntityMap_1_8.rewriteEntityIdArray( wrapper, oldId, newId, readerIndexAfterPID );
                break;
            case 0x00 /* Spawn Object : PacketPlayOutSpawnEntity */:
                EntityMap_1_9.rewriteSpawnObject( wrapper, oldId, newId );
                break;
            case 0x05 /* Spawn Player : PacketPlayOutNamedEntitySpawn */:
                EntityMap_1_8.rewriteSpawnPlayerUuid( wrapper, readerIndex );
                break;
            case 0x2F /* Combat Event : PacketPlayOutCombatEvent */:
                EntityMap_1_8.rewriteCombatEvent( wrapper, oldId, newId );
                break;
            case 0x3F /* EntityMetadata : PacketPlayOutEntityMetadata */:
                DefinedPacket.skipVarInt( packet ); // Entity ID
                rewriteMetaVarInt( wrapper, oldId + 1, newId + 1, 6, protocolVersion ); // fishing hook
                rewriteMetaVarInt( wrapper, oldId, newId, 7, protocolVersion ); // fireworks (et al)
                rewriteMetaVarInt( wrapper, oldId, newId, 13, protocolVersion ); // guardian beam
                break;
        }
        packet.readerIndex( readerIndex );
    }

    @Override
    public void rewriteServerbound(PacketWrapper wrapper, int oldId, int newId)
    {
        super.rewriteServerbound( wrapper, oldId, newId );
        ByteBuf packet = wrapper.buf;

        // Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int readerIndexAfterPID = packet.readerIndex();

        if ( packetId == 0x28 /* Spectate : PacketPlayInSpectate */ && !BungeeCord.getInstance().getConfig().isIpForward() )
        {
            EntityMap_1_8.rewriteSpectateUuid( wrapper, readerIndex, readerIndexAfterPID );
        }
        packet.readerIndex( readerIndex );
    }
}
