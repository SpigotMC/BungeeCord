package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.nbt.Tag;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistryData extends DefinedPacket
{

    private String registryId;
    private List<RegistryEntry> entries;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        registryId = readString( buf );
        int amount = readVarInt( buf );
        List<RegistryEntry> entries = new ArrayList<>();
        for( int i = 0; i < amount; i++ )
        {
            entries.add( new RegistryEntry( readString( buf ), readNullable( input -> readTag( input, protocolVersion ), buf ) ) );
        }
        this.entries = entries;
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( registryId, buf );
        writeVarInt( entries.size(), buf );
        for ( RegistryEntry entry : entries )
        {
            writeString( entry.entryId, buf );
            writeNullable( entry.tag, (tag, byteBuf) -> writeTag( tag, byteBuf, protocolVersion ), buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistryEntry
    {
        private String entryId;
        private Tag tag;
    }
}
