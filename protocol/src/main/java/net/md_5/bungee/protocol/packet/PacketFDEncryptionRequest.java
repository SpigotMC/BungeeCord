package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketFDEncryptionRequest extends DefinedPacket
{

    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;

    private PacketFDEncryptionRequest()
    {
        super( 0xFD );
    }

    public PacketFDEncryptionRequest(String serverId, byte[] publicKey, byte[] verifyToken)
    {
        this();
        this.serverId = serverId;
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
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
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
