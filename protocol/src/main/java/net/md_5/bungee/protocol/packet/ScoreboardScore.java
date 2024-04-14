package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.data.NumberFormat;
import net.md_5.bungee.protocol.util.ChatComponentDeserializable;
import net.md_5.bungee.protocol.util.ChatDeserializable;

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
    private ChatDeserializable displayNameRaw;
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
            displayNameRaw = readNullable( (b) -> readBaseComponent( b, protocolVersion ), buf );
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
            writeNullable( displayNameRaw, (s, b) -> DefinedPacket.writeBaseComponent( s, b, protocolVersion ), buf );
            writeNullable( numberFormat, (s, b) -> DefinedPacket.writeNumberFormat( s, b, protocolVersion ), buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public ScoreboardScore(String itemName, byte action, String scoreName, int value, BaseComponent displayName, NumberFormat numberFormat)
    {
        this.itemName = itemName;
        this.action = action;
        this.scoreName = scoreName;
        this.value = value;
        setDisplayName( displayName );
        this.numberFormat = numberFormat;
    }

    public BaseComponent getDisplayName()
    {
        if ( displayNameRaw == null )
        {
            return null;
        }
        return displayNameRaw.get();
    }

    public void setDisplayName(BaseComponent displayName)
    {
        if ( displayName == null )
        {
            this.displayNameRaw = null;
            return;
        }
        this.displayNameRaw = new ChatComponentDeserializable( displayName );
    }
}
