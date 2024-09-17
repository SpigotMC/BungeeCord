package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
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
public class Respawn extends DefinedPacket
{

    private Object dimension;
    private String worldName;
    private long seed;
    private short difficulty;
    private short gameMode;
    private short previousGameMode;
    private String levelType;
    private boolean debug;
    private boolean flat;
    private byte copyMeta;
    private Location deathLocation;
    private int portalCooldown;
    private int seaLevel;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 )
            {
                dimension = readVarInt( buf );
            } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 && protocolVersion < ProtocolConstants.MINECRAFT_1_19 )
            {
                dimension = readTag( buf, protocolVersion );
            } else
            {
                dimension = readString( buf );
            }
            worldName = readString( buf );
        } else
        {
            dimension = buf.readInt();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            seed = buf.readLong();
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
        {
            difficulty = buf.readUnsignedByte();
        }
        gameMode = buf.readUnsignedByte();
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            previousGameMode = buf.readUnsignedByte();
            debug = buf.readBoolean();
            flat = buf.readBoolean();
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                copyMeta = buf.readByte();
            }
        } else
        {
            levelType = readString( buf );
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
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_2 )
        {
            copyMeta = buf.readByte();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 )
            {
                writeVarInt( (Integer) dimension, buf );
            } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 && protocolVersion < ProtocolConstants.MINECRAFT_1_19 )
            {
                writeTag( (Tag) dimension, buf, protocolVersion );
            } else
            {
                writeString( (String) dimension, buf );
            }
            writeString( worldName, buf );
        } else
        {
            buf.writeInt( ( (Integer) dimension ) );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            buf.writeLong( seed );
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
        {
            buf.writeByte( difficulty );
        }
        buf.writeByte( gameMode );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            buf.writeByte( previousGameMode );
            buf.writeBoolean( debug );
            buf.writeBoolean( flat );
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_2 )
            {
                buf.writeByte( copyMeta );
            }
        } else
        {
            writeString( levelType, buf );
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
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_2 )
        {
            buf.writeByte( copyMeta );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
