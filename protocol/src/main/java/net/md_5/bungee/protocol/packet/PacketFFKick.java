package net.md_5.bungee.protocol.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.packet.PacketHandler;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketFFKick extends DefinedPacket
{

    public String message;

    public PacketFFKick(String message)
    {
        super( 0xFF );
        writeString( message );
    }

    PacketFFKick(byte[] buf)
    {
        super( 0xFF, buf );
        this.message = readUTF();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
