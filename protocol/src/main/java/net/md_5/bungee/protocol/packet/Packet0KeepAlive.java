package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Packet0KeepAlive extends DefinedPacket
{

    private int id;

    @Override
    public void read(ByteBuf buf)
    {
        id = buf.readInt();
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeInt( id );
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
