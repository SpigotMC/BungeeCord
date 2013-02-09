package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class Packet0KeepAlive extends DefinedPacket
{

    public int id;

    public Packet0KeepAlive(byte[] buffer)
    {
        super( 0x00, buffer );
        id = readInt();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
