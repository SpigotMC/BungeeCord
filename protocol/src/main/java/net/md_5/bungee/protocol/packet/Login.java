package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Location;
import net.md_5.bungee.protocol.ProtocolConstants;
import se.llbit.nbt.Tag;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Login extends DefinedPacket
{

    private int entityId;
    private boolean hardcore;
    private short gameMode;
    private short previousGameMode;
    private Set<String> worldNames;
    private Tag dimensions;
    private Object dimension;
    private String worldName;
    private long seed;
    private short difficulty;
    private int maxPlayers;
    private String levelType;
    private int viewDistance;
    private int simulationDistance;
    private boolean reducedDebugInfo;
    private boolean normalRespawn;
    private boolean limitedCrafting;
    private boolean debug;
    private boolean flat;
    private Location deathLocation;
    private int portalCooldown;
    private int seaLevel;
    private boolean secureProfile;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        entityId = buf.readInt();
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 )
        {
            hardcore = buf.readBoolean();
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
        {
            gameMode = buf.readUnsignedByte();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                previousGameMode = buf.readUnsignedByte();
            }

            worldNames = new HashSet<>();
            int worldCount = readVarInt( buf );
            for ( int i = 0; i < worldCount; i++ )
            {
                worldNames.add( readString( buf ) );
            }

            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                dimensions = readTag( buf, protocolVersion );
            }
        }

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 && protocolVersion < ProtocolConstants.MINECRAFT_1_19 )
            {
                dimension = readTag( buf, protocolVersion );
            } else if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                dimension = readString( buf );
            }
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                worldName = readString( buf );
            }
        } else if ( protocolVersion > ProtocolConstants.MINECRAFT_1_9 )
        {
            dimension = buf.readInt();
        } else
        {
            dimension = (int) buf.readByte();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 && protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
        {
            seed = buf.readLong();
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
        {
            difficulty = buf.readUnsignedByte();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 )
        {
            maxPlayers = readVarInt( buf );
        } else
        {
            maxPlayers = buf.readUnsignedByte();
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_16 )
        {
            levelType = readString( buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_14 )
        {
            viewDistance = readVarInt( buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_18 )
        {
            simulationDistance = readVarInt( buf );
        }
        if ( protocolVersion >= 29 )
        {
            reducedDebugInfo = buf.readBoolean();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            normalRespawn = buf.readBoolean();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_2 )
        {
            limitedCrafting = buf.readBoolean();
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 )
            {
                dimension = readVarInt( buf );
            } else
            {
                dimension = readString( buf );
            }
            worldName = readString( buf );
            seed = buf.readLong();
            gameMode = buf.readUnsignedByte();
            previousGameMode = buf.readByte();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            debug = buf.readBoolean();
            flat = buf.readBoolean();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            if ( buf.readBoolean() )
            {
                deathLocation = new Location( readString( buf ), buf.readLong() );
            }
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20 )
        {
            portalCooldown = readVarInt( buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_21_2 )
        {
            seaLevel = readVarInt( buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 )
        {
            secureProfile = buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeInt( entityId );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 )
        {
            buf.writeBoolean( hardcore );
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
        {
            buf.writeByte( gameMode );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                buf.writeByte( previousGameMode );
            }

            writeVarInt( worldNames.size(), buf );
            for ( String world : worldNames )
            {
                writeString( world, buf );
            }

            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                writeTag( dimensions, buf, protocolVersion );
            }
        }

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 && protocolVersion < ProtocolConstants.MINECRAFT_1_19 )
            {
                writeTag( (Tag) dimension, buf, protocolVersion );
            } else if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                writeString( (String) dimension, buf );
            }
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                writeString( worldName, buf );
            }
        } else if ( protocolVersion > ProtocolConstants.MINECRAFT_1_9 )
        {
            buf.writeInt( (Integer) dimension );
        } else
        {
            buf.writeByte( (Integer) dimension );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                buf.writeLong( seed );
            }
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
        {
            buf.writeByte( difficulty );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 )
        {
            writeVarInt( maxPlayers, buf );
        } else
        {
            buf.writeByte( maxPlayers );
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_16 )
        {
            writeString( levelType, buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_14 )
        {
            writeVarInt( viewDistance, buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_18 )
        {
            writeVarInt( simulationDistance, buf );
        }
        if ( protocolVersion >= 29 )
        {
            buf.writeBoolean( reducedDebugInfo );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            buf.writeBoolean( normalRespawn );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_2 )
        {
            buf.writeBoolean( limitedCrafting );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 )
            {
                writeVarInt( (Integer) dimension, buf );
            } else
            {
                writeString( (String) dimension, buf );
            }
            writeString( worldName, buf );
            buf.writeLong( seed );
            buf.writeByte( gameMode );
            buf.writeByte( previousGameMode );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            buf.writeBoolean( debug );
            buf.writeBoolean( flat );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            if ( deathLocation != null )
            {
                buf.writeBoolean( true );
                writeString( deathLocation.getDimension(), buf );
                buf.writeLong( deathLocation.getPos() );
            } else
            {
                buf.writeBoolean( false );
            }
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20 )
        {
            writeVarInt( portalCooldown, buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_21_2 )
        {
            writeVarInt( seaLevel, buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 )
        {
            buf.writeBoolean( secureProfile );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
