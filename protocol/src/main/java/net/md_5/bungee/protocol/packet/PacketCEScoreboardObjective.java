package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketCEScoreboardObjective extends DefinedPacket
{

    private String name;
    private String text;
    /**
     * 0 to create, 1 to remove.
     */
    private byte action;

    PacketCEScoreboardObjective()
    {
        super( 0xCE );
    }

    @Override
    public void read(ByteBuf buf)
    {
        name = readString( buf );
        text = readString( buf );
        action = buf.readByte();
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString( name, buf );
        writeString( text, buf );
        buf.writeByte( action );
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
