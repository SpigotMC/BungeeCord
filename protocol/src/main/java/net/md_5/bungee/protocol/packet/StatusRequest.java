package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StatusRequest extends DefinedPacket
{

    @Override
    public void read(ByteBuf buf)
    {
    }

    @Override
    public void write(ByteBuf buf)
    {
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
