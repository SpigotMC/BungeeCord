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
public class PlayerChat extends DefinedPacket
{

    private static final UUID EMPTY_UUID = new UUID( 0L, 0L );
    private String signedContent;
    private String unsignedContent; // nullable
    private UUID sender;
    private int typeId;
    private String displayName;
    private String teamName; // nullable
    private long timestamp;
    private long salt;
    private byte[] signature;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        signedContent = readString( buf, 262144 );
        if ( buf.readBoolean() )
        {
            unsignedContent = readString( buf, 262144 );
        }
        typeId = readVarInt( buf );
        sender = readUUID( buf );
        displayName = readString( buf, 262144 );
        if ( buf.readBoolean() )
        {
            teamName = readString( buf, 262144 );
        }
        timestamp = buf.readLong();
        salt = buf.readLong();
        signature = readArray( buf );
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( signedContent, buf );
        if ( unsignedContent != null )
        {
            buf.writeBoolean( true );
            writeString( unsignedContent, buf );
        } else
        {
            buf.writeBoolean( false );
        }
        writeVarInt( typeId, buf );
        writeUUID( sender, buf );
        writeString( displayName, buf );
        if ( teamName != null )
        {
            buf.writeBoolean( true );
            writeString( teamName, buf );
        } else
        {
            buf.writeBoolean( false );
        }
        buf.writeLong( timestamp );
        buf.writeLong( salt );
        writeArray( signature, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
