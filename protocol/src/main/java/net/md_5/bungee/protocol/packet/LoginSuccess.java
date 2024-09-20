package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoginSuccess extends DefinedPacket
{

    private UUID uuid;
    private String username;
    private Property[] properties;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            uuid = readUUID( buf );
        } else
        {
            uuid = UUID.fromString( readString( buf ) );
        }
        username = readString( buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            properties = readProperties( buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 && protocolVersion < ProtocolConstants.MINECRAFT_1_21_2 )
        {
            // Whether the client should disconnect on its own if it receives invalid data from the server
            buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            writeUUID( uuid, buf );
        } else
        {
            writeString( uuid.toString(), buf );
        }
        writeString( username, buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            writeProperties( properties, buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 && protocolVersion < ProtocolConstants.MINECRAFT_1_21_2 )
        {
            // Whether the client should disconnect on its own if it receives invalid data from the server
            // Vanilla sends true so we also send true
            buf.writeBoolean( true );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
