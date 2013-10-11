package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EncryptionRequest extends DefinedPacket
{

    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;

    @Override
    public void read(ByteBuf buf)
    {
        serverId = readString( buf );
        publicKey = readArray( buf );
        verifyToken = readArray( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString( serverId, buf );
        writeArray( publicKey, buf );
        writeArray( verifyToken, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
