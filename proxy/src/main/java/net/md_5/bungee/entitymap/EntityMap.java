package net.md_5.bungee.entitymap;

import com.flowpowered.nbt.stream.NBTInputStream;
import com.google.common.base.Throwables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

/**
 * Class to rewrite integers within packets.
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class EntityMap
{

    private final boolean[] clientboundInts = new boolean[ 256 ];
    private final boolean[] clientboundVarInts = new boolean[ 256 ];

    private final boolean[] serverboundInts = new boolean[ 256 ];
    private final boolean[] serverboundVarInts = new boolean[ 256 ];

    // Returns the correct entity map for the protocol version
    public static EntityMap getEntityMap(int version)
    {
        switch ( version )
        {
            case ProtocolConstants.MINECRAFT_1_8:
                return EntityMap_1_8.INSTANCE;
            case ProtocolConstants.MINECRAFT_1_9:
            case ProtocolConstants.MINECRAFT_1_9_1:
            case ProtocolConstants.MINECRAFT_1_9_2:
                return EntityMap_1_9.INSTANCE;
            case ProtocolConstants.MINECRAFT_1_9_4:
                return EntityMap_1_9_4.INSTANCE;
            case ProtocolConstants.MINECRAFT_1_10:
                return EntityMap_1_10.INSTANCE;
            case ProtocolConstants.MINECRAFT_1_11:
            case ProtocolConstants.MINECRAFT_1_11_1:
                return EntityMap_1_11.INSTANCE;
        }
        throw new RuntimeException( "Version " + version + " has no entity map" );
    }

    protected void addRewrite(int id, ProtocolConstants.Direction direction, boolean varint)
    {
        if ( direction == ProtocolConstants.Direction.TO_CLIENT )
        {
            if ( varint )
            {
                clientboundVarInts[id] = true;
            } else
            {
                clientboundInts[id] = true;
            }
        } else if ( varint )
        {
            serverboundVarInts[id] = true;
        } else
        {
            serverboundInts[id] = true;
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
            ByteBuf data = packet.copy();
            packet.readerIndex( offset );
            packet.writerIndex( offset );
            DefinedPacket.writeVarInt( readId == oldId ? newId : oldId, packet );
            packet.writeBytes( data );
            data.release();
        }
    }

    protected static void rewriteMetaVarInt(ByteBuf packet, int oldId, int newId, int metaIndex)
    {
        int readerIndex = packet.readerIndex();

        short index;
        while ( ( index = packet.readUnsignedByte() ) != 0xFF )
        {
            int type = DefinedPacket.readVarInt( packet );

            switch ( type )
            {
                case 0:
                    packet.skipBytes( 1 ); // byte
                    break;
                case 1:
                    if ( index == metaIndex )
                    {
                        int position = packet.readerIndex();
                        rewriteVarInt( packet, oldId, newId, position );
                        packet.readerIndex( position );
                    }
                    DefinedPacket.readVarInt( packet );
                    break;
                case 2:
                    packet.skipBytes( 4 ); // float
                    break;
                case 3:
                case 4:
                    DefinedPacket.readString( packet );
                    break;
                case 5:
                    if ( packet.readShort() != -1 )
                    {
                        packet.skipBytes( 3 ); // byte, short

                        int position = packet.readerIndex();
                        if ( packet.readByte() != 0 )
                        {
                            packet.readerIndex( position );

                            try
                            {
                                new NBTInputStream( new ByteBufInputStream( packet ), false ).readTag();
                            } catch ( IOException ex )
                            {
                                throw Throwables.propagate( ex );
                            }
                        }
                    }
                    break;
                case 6:
                    packet.skipBytes( 1 ); // boolean
                    break;
                case 7:
                    packet.skipBytes( 12 ); // float, float, float
                    break;
                case 8:
                    packet.readLong();
                    break;
                case 9:
                    if ( packet.readBoolean() )
                    {
                        packet.skipBytes( 8 ); // long
                    }
                    break;
                case 10:
                    DefinedPacket.readVarInt( packet );
                    break;
                case 11:
                    if ( packet.readBoolean() )
                    {
                        packet.skipBytes( 16 ); // long, long
                    }
                    break;
                case 12:
                    DefinedPacket.readVarInt( packet );
                    break;
                default:
                    throw new IllegalArgumentException( "Unknown meta type " + type );
            }
        }

        packet.readerIndex( readerIndex );
    }

    // Handles simple packets
    private static void rewrite(ByteBuf packet, int oldId, int newId, boolean[] ints, boolean[] varints)
    {
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;

        if ( ints[packetId] )
        {
            rewriteInt( packet, oldId, newId, readerIndex + packetIdLength );
        } else if ( varints[packetId] )
        {
            rewriteVarInt( packet, oldId, newId, readerIndex + packetIdLength );
        }
        packet.readerIndex( readerIndex );
    }
}
