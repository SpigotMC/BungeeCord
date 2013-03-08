package net.md_5.bungee.packet;

import io.netty.buffer.ByteBuf;
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
        writeString( message );
    }

    public Packet3Chat(ByteBuf buf)
    {
        super( 0x03, buf );
        this.message = readString();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
