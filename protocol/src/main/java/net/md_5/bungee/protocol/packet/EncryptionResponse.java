package net.md_5.bungee.protocol.packet;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
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
public class EncryptionResponse extends DefinedPacket
{

    /**
     * Size of the HMAC-SHA256 integrity signature since 26.4.
     */
    public static final int SIGNATURE_LENGTH = 32;

    private byte[] sharedSecret;
    private byte[] verifyToken;
    private EncryptionData encryptionData;
    /**
     * X.509 encoded X25519 key of the client, only used since 26.4.
     */
    private byte[] publicKey;
    /**
     * HMAC-SHA256 over the negotiated session parameters, only used since
     * 26.4.
     */
    private byte[] signature;

    public EncryptionResponse(byte[] sharedSecret, byte[] verifyToken, EncryptionData encryptionData)
    {
        this( sharedSecret, verifyToken, encryptionData, null, null );
    }

    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_26_4 )
        {
            publicKey = readArray( buf, EncryptionRequest.MAX_KEY_LENGTH );
            signature = new byte[ SIGNATURE_LENGTH ];
            buf.readBytes( signature );
            return;
        }
        sharedSecret = readArray( buf, 128 );
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_19 || protocolVersion >= ProtocolConstants.MINECRAFT_1_19_3 || buf.readBoolean() )
        {
            verifyToken = readArray( buf, 128 );
        } else
        {
            encryptionData = new EncryptionData( buf.readLong(), readArray( buf ) );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_26_4 )
        {
            Preconditions.checkArgument( signature.length == SIGNATURE_LENGTH, "Signature must be %s bytes", SIGNATURE_LENGTH );
            writeArray( publicKey, buf );
            buf.writeBytes( signature );
            return;
        }
        writeArray( sharedSecret, buf );
        if ( verifyToken != null )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 && protocolVersion <= ProtocolConstants.MINECRAFT_1_19_3 )
            {
                buf.writeBoolean( true );
            }
            writeArray( verifyToken, buf );
        } else
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 && protocolVersion <= ProtocolConstants.MINECRAFT_1_19_3 )
            {
                buf.writeBoolean( false );
            }
            buf.writeLong( encryptionData.getSalt() );
            writeArray( encryptionData.getSignature(), buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Data
    public static class EncryptionData
    {

        private final long salt;
        private final byte[] signature;
    }
}
