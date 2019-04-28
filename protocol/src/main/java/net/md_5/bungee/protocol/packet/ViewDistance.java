package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ViewDistance extends DefinedPacket
{

    private int distance;

    @Override
    public void read(ByteBuf buf)
    {
        distance = DefinedPacket.readVarInt( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        DefinedPacket.writeVarInt( distance, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
