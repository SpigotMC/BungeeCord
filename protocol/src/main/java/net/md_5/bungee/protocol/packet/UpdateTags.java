package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UpdateTags extends DefinedPacket
{

    private Map<String, Map<String, int[]>> tags;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        int amount = readVarInt( buf );
        Map<String, Map<String, int[]>> tags = new HashMap<>( amount );

        for (int i = 0; i < amount; i++ )
        {
            String key = readString( buf );
            int innerAmount = readVarInt( buf );
            Map<String, int[]> innerTags = new HashMap<>( innerAmount );
            for (int j = 0; j < innerAmount; j++ )
            {
                innerTags.put( readString( buf ), readVarIntArray( buf ) );
            }
            tags.put( key, innerTags );
        }

        this.tags = tags;
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeVarInt( tags.size(), buf );
        for ( Map.Entry<String, Map<String, int[]>> entry : tags.entrySet() )
        {
            writeString( entry.getKey(), buf );
            writeVarInt( entry.getValue().size(), buf );
            for ( Map.Entry<String, int[]> innerEntry : entry.getValue().entrySet() )
            {
                writeString( innerEntry.getKey(), buf );
                writeVarIntArray( innerEntry.getValue(), buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

}
