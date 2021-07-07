package ru.leymooo.botfilter.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.IntTag;
import se.llbit.nbt.NamedTag;

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
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version)
    {
        buf.writeByte( this.windowId );

        if ( version >= ProtocolConstants.MINECRAFT_1_17_1 )
        {
            DefinedPacket.writeVarInt( 0, buf );
        }

        buf.writeShort( this.slot );
        int id = this.item == 358 ? getCapthcaId( version ) : this.item;
        boolean present = id > 0;

        if ( version >= ProtocolConstants.MINECRAFT_1_13_2 )
        {
            buf.writeBoolean( present );
        }

        if ( !present && version < ProtocolConstants.MINECRAFT_1_13_2 )
        {
            buf.writeShort( -1 );
        }

        if ( present )
        {
            if ( version < ProtocolConstants.MINECRAFT_1_13_2 )
            {
                buf.writeShort( id );
            } else
            {
                DefinedPacket.writeVarInt( id, buf );
            }
            buf.writeByte( this.count );
            if ( version < ProtocolConstants.MINECRAFT_1_13 )
            {
                buf.writeShort( this.data );
            }

            if ( version < ProtocolConstants.MINECRAFT_1_17 )
            {
                buf.writeByte( 0 ); //No Nbt
            } else
            {
                CompoundTag nbt = new CompoundTag();
                nbt.add( "map", new IntTag( 0 ) );
                DefinedPacket.writeTag( new NamedTag( "", nbt ), buf );
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
        } else if ( version <= ProtocolConstants.MINECRAFT_1_16_4 )
        {
            return 733;
        } else
        {
            return 847;
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }
}
