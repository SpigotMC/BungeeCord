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



@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JoinGame extends DefinedPacket
{

    private final int entityId;
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

    private byte[] dimensions = new byte[] {
        10, 0, 0, 9, 0, 9, 100, 105, 109, 101, 110, 115, 105, 111, 110, 10, 0, 0, 0, 4, 8, 0, 4, 110, 97, 109, 101, 0, 19, 109, 105, 110, 101, 99, 114, 97, 102, 116, 58, 111, 118, 101, 114, 119, 111, 114, 108, 100, 1, 0, 9, 98, 101, 100, 95, 119, 111, 114, 107, 115, 1, 1, 0, 6, 115, 104, 114, 117, 110, 107, 0, 1, 0, 11, 112, 105, 103, 108, 105, 110, 95, 115, 97, 102, 101, 0, 1, 0, 11, 104, 97, 115, 95, 99, 101, 105, 108, 105, 110, 103, 0, 1, 0, 12, 104, 97, 115, 95, 115, 107, 121, 108, 105, 103, 104, 116, 1, 8, 0, 10, 105, 110, 102, 105, 110, 105, 98, 117, 114, 110, 0, 30, 109, 105, 110, 101, 99, 114, 97, 102, 116, 58, 105, 110, 102, 105, 110, 105, 98, 117, 114, 110, 95, 111, 118, 101, 114, 119, 111, 114, 108, 100, 1, 0, 9, 117, 108, 116, 114, 97, 119, 97, 114, 109, 0, 5, 0, 13, 97, 109, 98, 105, 101, 110, 116, 95, 108, 105, 103, 104, 116, 0, 0, 0, 0, 3, 0, 14, 108, 111, 103, 105, 99, 97, 108, 95, 104, 101, 105, 103, 104, 116, 0, 0, 1, 0, 1, 0, 9, 104, 97, 115, 95, 114, 97, 105, 100, 115, 1, 1, 0, 7, 110, 97, 116, 117, 114, 97, 108, 1, 1, 0, 20, 114, 101, 115, 112, 97, 119, 110, 95, 97, 110, 99, 104, 111, 114, 95, 119, 111, 114, 107, 115, 0, 0, 8, 0, 4, 110, 97, 109, 101, 0, 25, 109, 105, 110, 101, 99, 114, 97, 102, 116, 58, 111, 118, 101, 114, 119, 111, 114, 108, 100, 95, 99, 97, 118, 101, 115, 1, 0, 9, 98, 101, 100, 95, 119, 111, 114, 107, 115, 1, 1, 0, 6, 115, 104, 114, 117, 110, 107, 0, 1, 0, 11, 112, 105, 103, 108, 105, 110, 95, 115, 97, 102, 101, 0, 1, 0, 11, 104, 97, 115, 95, 99, 101, 105, 108, 105, 110, 103, 1, 1, 0, 12, 104, 97, 115, 95, 115, 107, 121, 108, 105, 103, 104, 116, 1, 8, 0, 10, 105, 110, 102, 105, 110, 105, 98, 117, 114, 110, 0, 30, 109, 105, 110, 101, 99, 114, 97, 102, 116, 58, 105, 110, 102, 105, 110, 105, 98, 117, 114, 110, 95, 111, 118, 101, 114, 119, 111, 114, 108, 100, 1, 0, 9, 117, 108, 116, 114, 97, 119, 97, 114, 109, 0, 5, 0, 13, 97, 109, 98, 105, 101, 110, 116, 95, 108, 105, 103, 104, 116, 0, 0, 0, 0, 3, 0, 14, 108, 111, 103, 105, 99, 97, 108, 95, 104, 101, 105, 103, 104, 116, 0, 0, 1, 0, 1, 0, 9, 104, 97, 115, 95, 114, 97, 105, 100, 115, 1, 1, 0, 7, 110, 97, 116, 117, 114, 97, 108, 1, 1, 0, 20, 114, 101, 115, 112, 97, 119, 110, 95, 97, 110, 99, 104, 111, 114, 95, 119, 111, 114, 107, 115, 0, 0, 8, 0, 10, 105, 110, 102, 105, 110, 105, 98, 117, 114, 110, 0, 27, 109, 105, 110, 101, 99, 114, 97, 102, 116, 58, 105, 110, 102, 105, 110, 105, 98, 117, 114, 110, 95, 110, 101, 116, 104, 101, 114, 1, 0, 9, 117, 108, 116, 114, 97, 119, 97, 114, 109, 1, 3, 0, 14, 108, 111, 103, 105, 99, 97, 108, 95, 104, 101, 105, 103, 104, 116, 0, 0, 0, -128, 1, 0, 7, 110, 97, 116, 117, 114, 97, 108, 0, 8, 0, 4, 110, 97, 109, 101, 0, 20, 109, 105, 110, 101, 99, 114, 97, 102, 116, 58, 116, 104, 101, 95, 110, 101, 116, 104, 101, 114, 1, 0, 9, 98, 101, 100, 95, 119, 111, 114, 107, 115, 0, 4, 0, 10, 102, 105, 120, 101, 100, 95, 116, 105, 109, 101, 0, 0, 0, 0, 0, 0, 70, 80, 1, 0, 6, 115, 104, 114, 117, 110, 107, 1, 1, 0, 11, 112, 105, 103, 108, 105, 110, 95, 115, 97, 102, 101, 1, 1, 0, 12, 104, 97, 115, 95, 115, 107, 121, 108, 105, 103, 104, 116, 0, 1, 0, 11, 104, 97, 115, 95, 99, 101, 105, 108, 105, 110, 103, 1, 5, 0, 13, 97, 109, 98, 105, 101, 110, 116, 95, 108, 105, 103, 104, 116, 61, -52, -52, -51, 1, 0, 9, 104, 97, 115, 95, 114, 97, 105, 100, 115, 0, 1, 0, 20, 114, 101, 115, 112, 97, 119, 110, 95, 97, 110, 99, 104, 111, 114, 95, 119, 111, 114, 107, 115, 1, 0, 8, 0, 10, 105, 110, 102, 105, 110, 105, 98, 117, 114, 110, 0, 24, 109, 105, 110, 101, 99, 114, 97, 102, 116, 58, 105, 110, 102, 105, 110, 105, 98, 117, 114, 110, 95, 101, 110, 100, 1, 0, 9, 117, 108, 116, 114, 97, 119, 97, 114, 109, 0, 3, 0, 14, 108, 111, 103, 105, 99, 97, 108, 95, 104, 101, 105, 103, 104, 116, 0, 0, 1, 0, 1, 0, 7, 110, 97, 116, 117, 114, 97, 108, 0, 8, 0, 4, 110, 97, 109, 101, 0, 17, 109, 105, 110, 101, 99, 114, 97, 102, 116, 58, 116, 104, 101, 95, 101, 110, 100, 1, 0, 9, 98, 101, 100, 95, 119, 111, 114, 107, 115, 0, 4, 0, 10, 102, 105, 120, 101, 100, 95, 116, 105, 109, 101, 0, 0, 0, 0, 0, 0, 23, 112, 1, 0, 6, 115, 104, 114, 117, 110, 107, 0, 1, 0, 11, 112, 105, 103, 108, 105, 110, 95, 115, 97, 102, 101, 0, 1, 0, 12, 104, 97, 115, 95, 115, 107, 121, 108, 105, 103, 104, 116, 0, 1, 0, 11, 104, 97, 115, 95, 99, 101, 105, 108, 105, 110, 103, 0, 5, 0, 13, 97, 109, 98, 105, 101, 110, 116, 95, 108, 105, 103, 104, 116, 0, 0, 0, 0, 1, 0, 9, 104, 97, 115, 95, 114, 97, 105, 100, 115, 1, 1, 0, 20, 114, 101, 115, 112, 97, 119, 110, 95, 97, 110, 99, 104, 111, 114, 95, 119, 111, 114, 107, 115, 0, 0, 0
    };

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
        buf.writeByte( gameMode );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            buf.writeByte( previousGameMode );

            writeVarInt( worldNames.size(), buf );
            for ( String world : worldNames )
            {
                writeString( world, buf );
            }

            buf.writeBytes( dimensions );
        }

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            writeString( (String) "minecraft:overworld", buf );
            writeString( worldName, buf );
        } else if ( protocolVersion > ProtocolConstants.MINECRAFT_1_9 )
        {
            buf.writeInt( 0 );
        } else
        {
            buf.writeByte( 0 );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            buf.writeLong( seed );
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
        {
            buf.writeByte( difficulty );
        }
        buf.writeByte( maxPlayers );
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_16 )
        {
            writeString( levelType, buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_14 )
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
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        throw new UnsupportedOperationException();
    }
}
