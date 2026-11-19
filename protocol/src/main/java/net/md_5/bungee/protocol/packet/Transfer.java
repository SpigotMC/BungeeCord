package net.md_5.bungee.protocol.packet;

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
public class Transfer extends DefinedPacket
{

    private String host;
    private int port;
    /**
     * Additional connection properties, which the client will append to the
     * host of its next handshake (host?key=value). Only sent to 26.4+ clients.
     */
    private Map<String, String> properties;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        host = readString( buf );
        port = readVarInt( buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_26_4 )
        {
            int size = readVarInt( buf );
            properties = new HashMap<>();
            for ( int i = 0; i < size; i++ )
            {
                properties.put( readString( buf ), readString( buf ) );
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( host, buf );
        writeVarInt( port, buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_26_4 )
        {
            writeVarInt( properties.size(), buf );
            for ( Map.Entry<String, String> property : properties.entrySet() )
            {
                writeString( property.getKey(), buf );
                writeString( property.getValue(), buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
