    package ru.leymooo.botfilter.packets;

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
public class SetSlot extends DefinedPacket
{

    private int windowId;
    private int slot;
    private int item;
    private int count;
    private int data;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeByte( this.windowId );
        buf.writeShort( this.slot );
        buf.writeShort( this.item == 358 && protocolVersion >= ProtocolConstants.MINECRAFT_1_13 ? 608 : this.item);
        if ( this.item >= 0 )
        {
            buf.writeByte( this.count );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
            {
                buf.writeByte( this.data );
            } else
            {
                buf.writeShort( this.data );
                buf.writeByte( 0 );
            }

        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }
}
