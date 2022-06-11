package ru.leymooo.botfilter.packets;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import ru.leymooo.botfilter.utils.DimensionCreator;
import se.llbit.nbt.Tag;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JoinGame extends DefinedPacket
{
    private final int entityId;
    private boolean hardcore = false;
    private short gameMode = 0;
    private short previousGameMode = 0;
    private Set<String> worldNames = new HashSet<>( Arrays.asList( "minecraft:overworld" ) );
    //private Tag dimensions;
    //private Object dimension;
    private String worldName = "minecraft:overworld";
    private long seed = 1;
    private short difficulty = 0;
    private short maxPlayers = 1;
    private String levelType = "flat";
    private int viewDistance = 1;
    private boolean reducedDebugInfo = false;
    private boolean normalRespawn = true;
    private boolean debug = false;
    private boolean flat = true;

    private Tag dimensions116 = DimensionCreator.OVERWORLD.getFullCodec( ProtocolConstants.MINECRAFT_1_16_1 );
    private Tag dimensions1162 = DimensionCreator.OVERWORLD.getFullCodec( ProtocolConstants.MINECRAFT_1_16_2 );
    private Tag dimensions1182 = DimensionCreator.OVERWORLD.getFullCodec( ProtocolConstants.MINECRAFT_1_18_2 );
    private Tag dimensions119 = DimensionCreator.OVERWORLD.getFullCodec( ProtocolConstants.MINECRAFT_1_19 );

    private Tag dimension = DimensionCreator.OVERWORLD.getAttributes( ProtocolConstants.MINECRAFT_1_16_2 );
    private Tag dimension1182 = DimensionCreator.OVERWORLD.getAttributes( ProtocolConstants.MINECRAFT_1_18_2 );
    public JoinGame()
    {
        entityId = 0;
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeInt( entityId );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 )
        {
            buf.writeBoolean( hardcore );
        }
        buf.writeByte( gameMode );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            buf.writeByte( previousGameMode );

            writeVarInt( worldNames.size(), buf );
            for ( String world : worldNames )
            {
                writeString( world, buf );
            }

            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
            {
                writeTag( dimensions119, buf );
            } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_18_2 )
            {
                writeTag( dimensions1182, buf );
            } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 )
            {
                writeTag( dimensions1162, buf );
            } else
            {
                writeTag( dimensions116, buf );
            }
        }

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 || protocolVersion <= ProtocolConstants.MINECRAFT_1_16_1 )
            {
                writeString( (String) "minecraft:overworld", buf );
            } else if ( protocolVersion == ProtocolConstants.MINECRAFT_1_18_2 )
            {
                writeTag( dimension1182, buf );
            } else
            {
                writeTag( dimension, buf );
            }
            writeString( worldName, buf );
        } else if ( protocolVersion > ProtocolConstants.MINECRAFT_1_9 )
        {
            buf.writeInt( 0 ); //dim
        } else
        {
            buf.writeByte( 0 ); //dim
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            buf.writeLong( seed );
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
            writeVarInt( viewDistance, buf );
        }
        if ( protocolVersion >= 29 )
        {
            buf.writeBoolean( reducedDebugInfo );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            buf.writeBoolean( normalRespawn );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            buf.writeBoolean( debug );
            buf.writeBoolean( flat );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            buf.writeBoolean( false ); //lastDeathPos
        }

    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        throw new UnsupportedOperationException();
    }
}
