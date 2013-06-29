package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
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

    private PacketCEScoreboardObjective()
    {
        super( 0xCE );
    }

    public PacketCEScoreboardObjective(String name, String text, byte action)
    {
        this();
        this.name = name;
        this.text = text;
        this.action = action;
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
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
