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
public class Respawn extends DefinedPacket
{

    private int dimension;
    private short difficulty;
    private short gameMode;
    private long hashedSeed;
    private String levelType;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        dimension = buf.readInt();
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
        {
            difficulty = buf.readUnsignedByte();
        } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            hashedSeed = buf.readLong();
        }
        gameMode = buf.readUnsignedByte();
        levelType = readString( buf );
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeInt( dimension );
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
        {
            buf.writeByte( difficulty );
        } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_15 )
        {
            buf.writeLong( hashedSeed );
        }
        buf.writeByte( gameMode );
        writeString( levelType, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
