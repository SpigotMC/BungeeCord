package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketD0DisplayScoreboard extends DefinedPacket
{

    /**
     * 0 = list, 1 = side, 2 = below.
     */
    private byte position;
    private String name;

    private PacketD0DisplayScoreboard()
    {
        super( 0xD0 );
    }

    @Override
    public void read(ByteBuf buf)
    {
        position = buf.readByte();
        name = readString( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeByte( position );
        writeString( name, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
