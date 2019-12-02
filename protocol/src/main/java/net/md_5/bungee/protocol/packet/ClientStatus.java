package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientStatus extends DefinedPacket<ClientStatus>
{

    private byte payload;

    @Override
    public void read(ByteBuf buf)
    {
        payload = buf.readByte();
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeByte( payload );
    }

    @Override
    public void callHandler(AbstractPacketHandler handler, PacketWrapper<ClientStatus> packet) throws Exception
    {
        handler.handleClientStatus( packet );
    }
}
