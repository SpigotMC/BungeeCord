package net.md_5.bungee.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    public PacketFFKick(ByteBuf buf)
    {
        super( 0xFF, buf );
        this.message = readString();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
