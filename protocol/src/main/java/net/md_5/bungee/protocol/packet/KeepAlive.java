package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class KeepAlive extends DefinedPacket
{

    private int randomId;

    @Override
    public void read(ByteBuf buf)
    {
        randomId = buf.readInt();
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeInt( randomId );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
