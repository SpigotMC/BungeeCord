package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EncryptionResponse extends DefinedPacket
{

    private byte[] sharedSecret;
    private byte[] verifyToken;

    @Override
    public void read(ByteBuf buf)
    {
        sharedSecret = readArray( buf );
        verifyToken = readArray( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeArray( sharedSecret, buf );
        writeArray( verifyToken, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
