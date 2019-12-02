package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LegacyPing extends DefinedPacket<LegacyPing>
{

    private final boolean v1_5;

    @Override
    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void callHandler(AbstractPacketHandler handler, PacketWrapper<LegacyPing> packet) throws Exception
    {
        handler.handleLegacyPing( packet );
    }
}
