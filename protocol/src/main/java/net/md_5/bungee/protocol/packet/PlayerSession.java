package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerSession extends DefinedPacket
{

    private UUID sessionId;
    private PlayerPublicKey publicKey;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        sessionId = readUUID( buf );
        publicKey = new PlayerPublicKey( buf.readLong(), readArray( buf, 512 ), readArray( buf, 4096 ) );
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeUUID( sessionId, buf );
        buf.writeLong( publicKey.getExpiry() );
        writeArray( publicKey.getKey(), buf );
        writeArray( publicKey.getSignature(), buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
