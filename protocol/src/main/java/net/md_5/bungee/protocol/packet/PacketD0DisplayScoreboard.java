package net.md_5.bungee.protocol.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.packet.PacketHandler;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketD0DisplayScoreboard extends DefinedPacket
{

    /**
     * 0 = list, 1 = side, 2 = below.
     */
    public byte position;
    public String name;

    public PacketD0DisplayScoreboard(byte[] buf)
    {
        super( 0xD0, buf );
        position = readByte();
        name = readUTF();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
