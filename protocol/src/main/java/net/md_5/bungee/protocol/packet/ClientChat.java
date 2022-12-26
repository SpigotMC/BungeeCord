package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ChatChain;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.SeenMessages;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientChat extends DefinedPacket
{

    private String message;
    private long timestamp;
    private long salt;
    private byte[] signature;
    private boolean signedPreview;
    private ChatChain chain;
    private SeenMessages seenMessages;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        message = readString( buf, 256 );
        timestamp = buf.readLong();
        salt = buf.readLong();

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_3 )
        {
            if ( buf.readBoolean() )
            {
                signature = new byte[ 256 ];
                buf.readBytes( signature );
            }
        } else
        {
            signature = readArray( buf );
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_19_3 )
        {
            signedPreview = buf.readBoolean();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_3 )
        {
            seenMessages = new SeenMessages();
            seenMessages.read( buf, direction, protocolVersion );
        } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 )
        {
            chain = new ChatChain();
            chain.read( buf, direction, protocolVersion );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( message, buf );
        buf.writeLong( timestamp );
        buf.writeLong( salt );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_3 )
        {
            buf.writeBoolean( signature != null );
            if ( signature != null )
            {
                buf.writeBytes( signature );
            }
        } else
        {
            writeArray( signature, buf );
        }
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_19_3 )
        {
            buf.writeBoolean( signedPreview );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_3 )
        {
            seenMessages.write( buf, direction, protocolVersion );
        } else if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 )
        {
            chain.write( buf, direction, protocolVersion );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
