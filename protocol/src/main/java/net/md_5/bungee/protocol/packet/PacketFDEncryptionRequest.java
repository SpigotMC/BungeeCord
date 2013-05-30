package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.packet.PacketHandler;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketFDEncryptionRequest extends DefinedPacket
{

    public String serverId;
    public byte[] publicKey;
    public byte[] verifyToken;

    public PacketFDEncryptionRequest(String serverId, byte[] publicKey, byte[] verifyToken)
    {
        super( 0xFD );
        writeString( serverId );
        writeArray( publicKey );
        writeArray( verifyToken );
        this.serverId = serverId;
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
    }

    PacketFDEncryptionRequest(byte[] buf)
    {
        super( 0xFD, buf );
        serverId = readUTF();
        publicKey = readArray();
        verifyToken = readArray();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
