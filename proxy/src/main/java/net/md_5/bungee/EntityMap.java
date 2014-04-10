package net.md_5.bungee;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.LoginRequest;

/**
 * Class to rewrite integers within packets.
 */
public class EntityMap
{

    private final boolean[] clientboundInts = new boolean[ 256 ];
    private final boolean[] clientboundVarInts = new boolean[ 256 ];

    private final boolean[] serverboundInts = new boolean[ 256 ];
    private final boolean[] serverboundVarInts = new boolean[ 256 ];

    private final int version;

    public EntityMap(int version)
    {
        this.version = version;

        clientboundInts[0x04] = true; // Entity Equipment
        clientboundInts[0x0A] = true; // Use bed
        clientboundVarInts[0x0B] = true; // Animation
        clientboundVarInts[0x0C] = true; // Spawn Player
        clientboundInts[0x0D] = true; // Collect Item
        clientboundVarInts[0x0E] = true; // Spawn Object
        clientboundVarInts[0x0F] = true; // Spawn Mob
        clientboundVarInts[0x10] = true; // Spawn Painting
        clientboundVarInts[0x11] = true; // Spawn Experience Orb
        clientboundInts[0x12] = true; // Entity Velocity
        clientboundInts[0x14] = true; // Entity
        clientboundInts[0x15] = true; // Entity Relative Move
        clientboundInts[0x16] = true; // Entity Look
        clientboundInts[0x17] = true; // Entity Look and Relative Move
        clientboundInts[0x18] = true; // Entity Teleport
        clientboundInts[0x19] = true; // Entity Head Look
        clientboundInts[0x1A] = true; // Entity Status
        clientboundInts[0x1B] = true; // Attach Entity
        clientboundInts[0x1C] = true; // Entity Metadata
        clientboundInts[0x1D] = true; // Entity Effect
        clientboundInts[0x1E] = true; // Remove Entity Effect
        clientboundInts[0x20] = true; // Entity Properties
        clientboundVarInts[0x25] = true; // Block Break Animation
        clientboundVarInts[0x2C] = true; // Spawn Global Entity

        serverboundInts[0x02] = true; // Use Entity
        serverboundInts[0x0A] = true; // Animation
        serverboundInts[0x0B] = true; // Entity Action

        if ( version >= 7 )
        {
            migrateIntToVarint( clientboundInts, clientboundVarInts );
            migrateIntToVarint( serverboundInts, serverboundVarInts );
        }
    }

    private void migrateIntToVarint(boolean[] ints, boolean[] varints)
    {
        for ( int i = 0; i < ints.length; i++ )
        {
            if ( ints[i] )
            {
                varints[i] = true;
                ints[i] = false;
            }
        }
    }

    public void rewriteServerbound(ByteBuf packet, int serverEntityId, int clientEntityId)
    {
        rewrite( packet, serverEntityId, clientEntityId, serverboundInts, serverboundVarInts );
    }

    public void rewriteClientbound(ByteBuf packet, int serverEntityId, int clientEntityId)
    {
        rewrite( packet, serverEntityId, clientEntityId, clientboundInts, clientboundVarInts );

        //Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
        if ( packetId == 0x0D /* Collect Item */ || packetId == 0x1B /* Attach Entity */ )
        {
            int readId = packet.getInt( packetIdLength + 4 );
            if ( readId == serverEntityId )
            {
                packet.setInt( packetIdLength + 4, clientEntityId );
            } else if ( readId == clientEntityId )
            {
                packet.setInt( packetIdLength + 4, serverEntityId );
            }
        } else if ( packetId == 0x13 /* Destroy Entities */ )
        {
            int count = packet.getByte( packetIdLength );
            for ( int i = 0; i < count; i++ )
            {
                int readId = packet.getInt( packetIdLength + 1 + i * 4 );
                if ( readId == serverEntityId )
                {
                    packet.setInt( packetIdLength + 1 + i * 4, clientEntityId );
                } else if ( readId == clientEntityId )
                {
                    packet.setInt( packetIdLength + 1 + i * 4, serverEntityId );
                }
            }
        } else if ( packetId == 0x0E /* Spawn Object */ )
        {
            DefinedPacket.readVarInt( packet );
            int idLength = packet.readerIndex() - readerIndex - packetIdLength;

            int type = packet.getByte( packetIdLength + idLength );

            if ( type == 60 || type == 90 )
            {
                int readId = packet.getInt( packetIdLength + idLength + 15 );
                int newId = -1;
                if ( readId == serverEntityId )
                {
                    packet.setInt( packetIdLength + idLength + 15, clientEntityId );
                    newId = clientEntityId;
                } else if ( readId == clientEntityId )
                {
                    packet.setInt( packetIdLength + idLength + 15, serverEntityId );
                    newId = clientEntityId;
                }
                if ( newId != -1 )
                {
                    if ( newId == 0 && readId != 0 )
                    { // Trim off the extra data
                        packet.readerIndex( readerIndex );
                        packet.writerIndex( packet.readableBytes() - 6 );
                    } else if ( newId != 0 && readId == 0 )
                    { // Add on the extra data
                        packet.readerIndex( readerIndex );
                        packet.capacity( packet.readableBytes() + 6 );
                        packet.writerIndex( packet.readableBytes() + 6 );
                    }
                }
            }
        } else if ( packetId == 0x0C /* Spawn Player */ && version == 5 )
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
                        DefinedPacket.writeString( username, packet);
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
