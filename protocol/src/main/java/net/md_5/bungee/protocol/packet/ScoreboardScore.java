package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.md_5.bungee.protocol.AbstractPacketHandler;

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
    public void read(ByteBuf buf)
    {
        itemName = readString( buf );
        action = buf.readByte();
        if ( action != 1 )
        {
            scoreName = readString( buf );
            value = buf.readInt();
        }
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString( itemName, buf );
        buf.writeByte( action );
        if ( action != 1 )
        {
            writeString( scoreName, buf );
            buf.writeInt( value );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
