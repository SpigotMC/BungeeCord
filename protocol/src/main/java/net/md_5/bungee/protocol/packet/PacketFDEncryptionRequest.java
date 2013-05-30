package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketFDEncryptionRequest extends DefinedPacket
{

    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;

    PacketFDEncryptionRequest()
    {
        super( 0xFD );
    }

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
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
