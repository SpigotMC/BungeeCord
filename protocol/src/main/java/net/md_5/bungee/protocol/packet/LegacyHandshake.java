package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LegacyHandshake extends DefinedPacket<LegacyHandshake>
{

    @Override
    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void write(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void callHandler(AbstractPacketHandler handler, PacketWrapper<LegacyHandshake> packet) throws Exception
    {
        handler.handleLegacyHandshake( packet );
    }
}
