package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.NumberFormat;
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
    private BaseComponent displayName;
    private NumberFormat numberFormat;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        itemName = readString( buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_3 )
        {
            action = 0;
        } else
        {
            action = buf.readByte();
        }
        scoreName = readString( buf );
        if ( action != 1 )
        {
            value = readVarInt( buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_3 )
        {
            displayName = readNullable( (b) -> readBaseComponent( b, protocolVersion ), buf );
            numberFormat = readNullable( (b) -> readNumberFormat( b, protocolVersion ), buf );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( itemName, buf );
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_20_3 )
        {
            buf.writeByte( action );
        }
        writeString( scoreName, buf );
        if ( action != 1 || protocolVersion >= ProtocolConstants.MINECRAFT_1_20_3 )
        {
            writeVarInt( value, buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_3 )
        {
            writeNullable( displayName, (s, b) -> DefinedPacket.writeBaseComponent( s, b, protocolVersion ), buf );
            writeNullable( numberFormat, (s, b) -> DefinedPacket.writeNumberFormat( s, b, protocolVersion ), buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
