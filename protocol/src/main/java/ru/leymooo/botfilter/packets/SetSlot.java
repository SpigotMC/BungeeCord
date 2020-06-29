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
        int id = this.item == 358 ? getCapthcaId( protocolVersion ) : this.item;
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13_2 )
        {
            buf.writeBoolean( item >= 1 );
            if ( item <= 0 )
            {
                return;
            }
            writeVarInt( id, buf );
        } else
        {
            buf.writeShort( id );
        }

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

    private int getCapthcaId(int version)
    {
        if ( version <= ProtocolConstants.MINECRAFT_1_12_2 )
        {
            return 358;
        } else if ( version == ProtocolConstants.MINECRAFT_1_13 )
        {
            return 608;
        } else if ( version <= ProtocolConstants.MINECRAFT_1_13_2 )
        {
            return 613;
        } else if ( version <= ProtocolConstants.MINECRAFT_1_15_2 )
        {
            return 671;
        } else
        {
            return 733;
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }
}
