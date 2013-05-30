package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class Packet0KeepAlive extends DefinedPacket
{

    private int id;

    private Packet0KeepAlive()
    {
        super( 0x00 );
    }

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
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
