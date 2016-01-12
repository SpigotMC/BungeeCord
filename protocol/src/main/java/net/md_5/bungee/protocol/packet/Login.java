package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Login extends DefinedPacket
{

    private int entityId;
    private int gameMode;
    private int dimension;
    private int difficulty;
    private int maxPlayers;
    private String levelType;
    private boolean reducedDebugInfo;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        entityId = buf.readInt();
        gameMode = buf.readUnsignedByte();
        dimension = buf.readByte();
        difficulty = buf.readUnsignedByte();
        maxPlayers = buf.readUnsignedByte();
        levelType = readString( buf );
        if ( protocolVersion >= 29 )
        {
            reducedDebugInfo = buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeInt( entityId );
        buf.writeByte( gameMode );
        buf.writeByte( dimension );
        buf.writeByte( difficulty );
        buf.writeByte( maxPlayers );
        writeString( levelType, buf );
        if ( protocolVersion >= 29 )
        {
            buf.writeBoolean( reducedDebugInfo );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
