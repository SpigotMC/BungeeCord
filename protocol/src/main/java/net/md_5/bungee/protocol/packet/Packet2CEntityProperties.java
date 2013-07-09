package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class Packet2CEntityProperties extends DefinedPacket
{

    public Packet2CEntityProperties()
    {
        super( 0x2C );
    }

    @Override
    public void read(ByteBuf buf)
    {
        buf.readInt();
        int recordCount = buf.readInt();
        for ( int i = 0; i < recordCount; i++ )
        {
            readString( buf );
            buf.readDouble();
            short size = buf.readShort();
            for ( short s = 0; s < size; s++ )
            {
                buf.skipBytes( 25 ); // long, long, double, byte
            }
        }
    }

    @Override
    public void write(ByteBuf buf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
