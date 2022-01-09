package net.md_5.bungee.entitymap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.ProtocolConstants;

class EntityMap_1_8 extends EntityMap
{

    static final EntityMap_1_8 INSTANCE = new EntityMap_1_8();

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
    public void rewriteClientbound(PacketWrapper wrapper, int oldId, int newId)
    {
        super.rewriteClientbound( wrapper, oldId, newId );
        ByteBuf packet = wrapper.buf;

        //Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int readerIndexAfterPID = packet.readerIndex();
        switch ( packetId )
        {
            case 0x1B /* Attach Entity */:
                rewriteInt( wrapper, oldId, newId, readerIndexAfterPID + 4 );
                break;
            case 0x0D /* Collect Item */:
                DefinedPacket.skipVarInt( packet );
                rewriteVarInt( wrapper, oldId, newId, packet.readerIndex() );
                break;
            case 0x13 /* Destroy Entities */:
                rewriteDestroyEntities( wrapper, oldId, newId, readerIndexAfterPID );
                break;
            case 0x0E /* Spawn Object */:
                DefinedPacket.skipVarInt( packet );
                int type = packet.readUnsignedByte();

                if ( type == 60 || type == 90 )
                {
                    packet.skipBytes( 14 );
                    int position = packet.readerIndex();
                    int readId = packet.readInt();
                    int changedId = readId;

                    if ( readId == oldId )
                    {
                        packet.setInt( position, changedId = newId );
                    } else if ( readId == newId )
                    {
                        packet.setInt( position, changedId = oldId );
                    }

                    if ( readId > 0 && changedId <= 0 )
                    {
                        packet.writerIndex( packet.writerIndex() - 6 );
                    } else if ( changedId > 0 && readId <= 0 )
                    {
                        packet.ensureWritable( 6 );
                        packet.writerIndex( packet.writerIndex() + 6 );
                    }
                }
                break;
            case 0x0C /* Spawn Player */:
                rewriteSpawnPlayerUuid( wrapper, readerIndex );
                break;
            case 0x42 /* Combat Event */:
                rewriteCombatEvent( wrapper, oldId, newId );
                break;
        }
        packet.readerIndex( readerIndex );
    }

    @Override
    public void rewriteServerbound(PacketWrapper wrapper, int oldId, int newId)
    {
        super.rewriteServerbound( wrapper, oldId, newId );
        ByteBuf packet = wrapper.buf;

        //Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int readerIndexAfterPID = packet.readerIndex();

        if ( packetId == 0x18 /* Spectate */ && !BungeeCord.getInstance().getConfig().isIpForward() )
        {
            rewriteSpectateUuid( wrapper, readerIndex, readerIndexAfterPID );
        }
        packet.readerIndex( readerIndex );
    }

    public static void rewriteCombatEvent(PacketWrapper wrapper, int oldId, int newId)
    {
        if ( oldId == newId )
        {
            return;
        }
        ByteBuf packet = wrapper.buf;

        int event = packet.readUnsignedByte();
        if ( event == 1 /* End Combat*/ )
        {
            DefinedPacket.skipVarInt( packet );
            rewriteInt( wrapper, oldId, newId, packet.readerIndex() );
        } else if ( event == 2 /* Entity Dead */ )
        {
            int position = packet.readerIndex();
            rewriteVarInt( wrapper, oldId, newId, packet.readerIndex() );
            packet.readerIndex( position );
            DefinedPacket.skipVarInt( packet );
            rewriteInt( wrapper, oldId, newId, packet.readerIndex() );
        }
    }

    public static void rewriteSpawnPlayerUuid(PacketWrapper wrapper, int readerIndex)
    {
        ByteBuf packet = wrapper.buf;

        DefinedPacket.skipVarInt( packet ); // Entity ID
        int readerIndexAfterEID = packet.readerIndex();
        UUID uuid = DefinedPacket.readUUID( packet );
        ProxiedPlayer player;
        if ( ( player = BungeeCord.getInstance().getPlayerByOfflineUUID( uuid ) ) != null )
        {
            wrapper.destroyCompressed();

            int previous = packet.writerIndex();
            packet.readerIndex( readerIndex );
            packet.writerIndex( readerIndexAfterEID );
            DefinedPacket.writeUUID( player.getUniqueId(), packet );
            packet.writerIndex( previous );
        }
    }

    public static void rewriteDestroyEntities(PacketWrapper wrapper, int oldId, int newId, int readerIndexAfterPID)
    {
        if ( oldId == newId )
        {
            return;
        }
        wrapper.destroyCompressed();

        ByteBuf packet = wrapper.buf;
        int count = DefinedPacket.readVarInt( packet );
        int[] ids = new int[ count ];
        for ( int i = 0; i < count; i++ )
        {
            ids[ i ] = DefinedPacket.readVarInt( packet );
        }
        packet.readerIndex( readerIndexAfterPID );
        packet.writerIndex( readerIndexAfterPID );
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
    }

    public static void rewriteSpectateUuid(PacketWrapper wrapper, int readerIndex, int readerIndexAfterPID)
    {
        ByteBuf packet = wrapper.buf;

        UUID uuid = DefinedPacket.readUUID( packet );
        ProxiedPlayer player;
        if ( ( player = BungeeCord.getInstance().getPlayer( uuid ) ) != null )
        {
            wrapper.destroyCompressed();

            int previous = packet.writerIndex();
            packet.readerIndex( readerIndex );
            packet.writerIndex( readerIndexAfterPID );
            DefinedPacket.writeUUID( ( (UserConnection) player ).getPendingConnection().getOfflineId(), packet );
            packet.writerIndex( previous );
        }
    }
}
