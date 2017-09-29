package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import java.util.concurrent.ThreadLocalRandom;
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
    private int count;
    private int item;
    private int data;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeByte( this.windowId );
        buf.writeShort( this.slot );
        buf.writeShort( this.item );
        if ( this.item != -1 )
        {
            buf.writeByte( this.count );
            buf.writeShort( this.data );
            buf.writeByte( 0 );
        }
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.skipBytes( buf.readableBytes() );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }

    public SetSlot updateSlotAndData()
    {
        this.slot++;
        this.data = ThreadLocalRandom.current().nextInt( 0, 15 );
        return this;
    }

    public SetSlot resetSlot()
    {
        this.slot--;
        return this;
    }

    public SetSlot resetItemData()
    {
        this.data = 0;
        this.item = -1;
        return this;
    }
}
