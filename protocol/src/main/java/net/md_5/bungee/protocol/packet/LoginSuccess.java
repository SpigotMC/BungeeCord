package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
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
public class LoginSuccess extends DefinedPacket
{

    private UUID uuid;
    private String username;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion <= ProtocolConstants.MINECRAFT_1_7_2 )
        {
            uuid = readUndashedUUID( buf );
        } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            uuid = readUUID( buf );
        } else
        {
            uuid = UUID.fromString( readString( buf ) );
        }
        username = readString( buf );
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion <= ProtocolConstants.MINECRAFT_1_7_2 )
        {
            writeUndashedUUID( uuid.toString(), buf );
        } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            writeUUID( uuid, buf );
        } else
        {
            writeString( uuid.toString(), buf );
        }
        writeString( username, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    // Thanks to Travertine https://github.com/PaperMC/Travertine/blob/master/Waterfall-Proxy-Patches/0003-1.7.x-Protocol-Patch.patch#L936
    private static UUID readUndashedUUID(ByteBuf buf)
    {
        return UUID.fromString( new StringBuilder( readString( buf ) ).insert( 20, '-' ).insert( 16, '-' ).insert( 12, '-' ).insert( 8, '-' ).toString() );
    }

    private static void writeUndashedUUID(String uuid, ByteBuf buf)
    {
        writeString( new StringBuilder( 32 ).append( uuid, 0, 8 ).append( uuid, 9, 13 ).append( uuid, 14, 18 ).append( uuid, 19, 23 ).append( uuid, 24, 36 ).toString(), buf );
    }
    // Travertine end
}
