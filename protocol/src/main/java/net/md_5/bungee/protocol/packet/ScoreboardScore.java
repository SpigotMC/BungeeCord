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
public class ScoreboardScore extends DefinedPacket
{

    private String itemName;
    /**
     * 0 = create / update, 1 = remove.
     */
    private byte action;
    private String scoreName;
    private int value;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        itemName = readString( buf );
        action = buf.readByte();
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_8 )
        {
            scoreName = readString( buf );
            if ( action != 1 )
            {
                value = readVarInt( buf );
            }
        } else
        {
            if ( action != 1 )
            {
                scoreName = readString( buf );
                value = buf.readInt();
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( itemName, buf );
        buf.writeByte( action );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_8 )
        {
            writeString( scoreName, buf );
            if ( action != 1 )
            {
                writeVarInt( value, buf );
            }
        } else
        {
            if ( action != 1 )
            {
                writeString( scoreName, buf );
                buf.writeInt( value );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
