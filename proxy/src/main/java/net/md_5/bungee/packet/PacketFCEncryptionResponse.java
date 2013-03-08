package net.md_5.bungee.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketFCEncryptionResponse extends DefinedPacket
{

    public byte[] sharedSecret;
    public byte[] verifyToken;

    public PacketFCEncryptionResponse()
    {
        super( 0xFC );
        writeArray( new byte[ 0 ] );
        writeArray( new byte[ 0 ] );
    }

    public PacketFCEncryptionResponse(byte[] sharedSecret, byte[] verifyToken)
    {
        super( 0xFC );
        writeArray( sharedSecret );
        writeArray( verifyToken );
    }

    public PacketFCEncryptionResponse(ByteBuf buf)
    {
        super( 0xFC, buf );
        this.sharedSecret = readArray();
        this.verifyToken = readArray();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
