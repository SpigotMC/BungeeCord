package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketFEPing extends DefinedPacket
{

    public PacketFEPing(byte[] buffer)
    {
        super( 0xFE, buffer );
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
