package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class Packet3Chat extends DefinedPacket
{

    public String message;

    public Packet3Chat(String message)
    {
        super( 0x03 );
        writeUTF( message );
        this.message = message;
    }

    Packet3Chat(byte[] buf)
    {
        super( 0x03, buf );
        this.message = readUTF();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
