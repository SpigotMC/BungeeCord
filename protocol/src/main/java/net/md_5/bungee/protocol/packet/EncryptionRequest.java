package net.md_5.bungee.protocol.packet;

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
public class EncryptionRequest extends DefinedPacket
{

    /**
     * Maximum length of the X.509 encoded key exchange key since 26.4.
     */
    public static final int MAX_KEY_LENGTH = 256 + 128;

    /**
     * Random session salt, only used since 26.4.
     */
    private long salt;
    private String serverId;
    /**
     * X.509 encoded RSA key before 26.4, X.509 encoded X25519 key since 26.4.
     */
    private byte[] publicKey;
    /**
     * Only used before 26.4.
     */
    private byte[] verifyToken;
    private boolean shouldAuthenticate;

    public EncryptionRequest(String serverId, byte[] publicKey, byte[] verifyToken, boolean shouldAuthenticate)
    {
        this( 0, serverId, publicKey, verifyToken, shouldAuthenticate );
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_26_4 )
        {
            salt = buf.readLong();
            serverId = readString( buf, 20 );
            publicKey = readArray( buf, MAX_KEY_LENGTH );
            shouldAuthenticate = buf.readBoolean();
            return;
        }
        serverId = readString( buf );
        publicKey = readArray( buf );
        verifyToken = readArray( buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 )
        {
            shouldAuthenticate = buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_26_4 )
        {
            buf.writeLong( salt );
            writeString( serverId, buf, 20 );
            writeArray( publicKey, buf );
            buf.writeBoolean( shouldAuthenticate );
            return;
        }
        writeString( serverId, buf );
        writeArray( publicKey, buf );
        writeArray( verifyToken, buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_5 )
        {
            buf.writeBoolean( shouldAuthenticate );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
