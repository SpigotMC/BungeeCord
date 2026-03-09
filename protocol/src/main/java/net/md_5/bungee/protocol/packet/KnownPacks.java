package net.md_5.bungee.protocol.packet;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class KnownPacks extends DefinedPacket
{

    private List<KnownPack> knownPacks;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        int amount = readVarInt( buf );
        Preconditions.checkState( amount >= 0 && ( direction == ProtocolConstants.Direction.TO_CLIENT || amount <= 64 ), "invalid known packs amount" );
        List<KnownPack> packs = new ArrayList<>( amount );
        for ( int i = 0; i < amount; i++ )
        {
            packs.add( new KnownPack( readString( buf ), readString( buf ), readString( buf ) ) );
        }
        knownPacks = packs;
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeVarInt( knownPacks.size(), buf );
        for ( KnownPack pack : knownPacks )
        {
            writeString( pack.getNamespace(), buf );
            writeString( pack.getId(), buf );
            writeString( pack.getVersion(), buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Data
    @AllArgsConstructor
    public static class KnownPack
    {
        private String namespace;
        private String id;
        private String version;
    }

}
