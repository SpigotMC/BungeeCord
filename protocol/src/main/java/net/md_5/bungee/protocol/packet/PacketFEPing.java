package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketFEPing extends DefinedPacket
{

    private byte version;

    PacketFEPing()
    {
        super( 0xFE );
    }

    @Override
    public void read(ByteBuf buf)
    {
        version = buf.readByte();
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeByte( version );
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
