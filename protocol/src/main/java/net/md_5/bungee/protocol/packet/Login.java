package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Login extends DefinedPacket
{

    private int entityId;
    private short gameMode;
    private int dimension;
    private long seed;
    private short difficulty;
    private short maxPlayers;
    private String levelType;
    private int viewDistance;
    private boolean reducedDebugInfo;
    private boolean normalRespawn;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        entityId = buf.readInt();
        gameMode = buf.readUnsignedByte();
        if ( protocolVersion > ProtocolConstants.MINECRAFT_1_9 )
        {
            dimension = buf.readInt();
        } else
        {
            dimension = buf.readByte();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            seed = buf.readLong();
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
        {
            difficulty = buf.readUnsignedByte();
        }
        maxPlayers = buf.readUnsignedByte();
        levelType = readString( buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_14 )
        {
            viewDistance = readVarInt( buf );
        }
        if ( protocolVersion >= 29 )
        {
            reducedDebugInfo = buf.readBoolean();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            normalRespawn = buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeInt( entityId );
        buf.writeByte( gameMode );
        if ( protocolVersion > ProtocolConstants.MINECRAFT_1_9 )
        {
            buf.writeInt( dimension );
        } else
        {
            buf.writeByte( dimension );
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
        writeString( levelType, buf );
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
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
