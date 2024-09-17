package net.md_5.bungee.protocol.packet;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
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
public class DisconnectReportDetails extends DefinedPacket
{

    private Map<String, String> details;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        int len = readVarInt( buf );
        Preconditions.checkArgument( len <= 32, "Too many details" );

        details = new HashMap<>();
        for ( int i = 0; i < len; i++ )
        {
            details.put( readString( buf, 128 ), readString( buf, 4096 ) );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        Preconditions.checkArgument( details.size() <= 32, "Too many details" );
        writeVarInt( details.size(), buf );

        for ( Map.Entry<String, String> detail : details.entrySet() )
        {
            writeString( detail.getKey(), buf, 128 );
            writeString( detail.getValue(), buf, 4096 );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
