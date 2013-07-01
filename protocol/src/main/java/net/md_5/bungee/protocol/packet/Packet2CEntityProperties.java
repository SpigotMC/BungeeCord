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

    private int entityId;
    private Map<String, Double> data = new HashMap<>();

    public Packet2CEntityProperties()
    {
        super( 0x2C );
    }

    @Override
    public void read(ByteBuf buf)
    {
        entityId = buf.readInt();
        int recordCount = buf.readInt();
        for ( int i = 0; i < recordCount; i++ )
        {
            data.put( readString( buf ), buf.readDouble() );
        }
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeInt( entityId );
        buf.writeInt( data.size() );
        for ( Map.Entry<String, Double> entry : data.entrySet() )
        {
            writeString( entry.getKey(), buf );
            buf.writeDouble( entry.getValue() );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
