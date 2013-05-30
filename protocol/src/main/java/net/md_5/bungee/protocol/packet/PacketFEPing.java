package net.md_5.bungee.protocol.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.packet.PacketHandler;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketFEPing extends DefinedPacket
{

    public byte version;

    PacketFEPing(byte[] buffer)
    {
        super( 0xFE, buffer );
        version = readByte();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
