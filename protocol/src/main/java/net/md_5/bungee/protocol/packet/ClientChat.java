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

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        message = readString( buf, 256 );
        timestamp = buf.readLong();
        salt = buf.readLong();
        signature = readArray( buf );
        signedPreview = buf.readBoolean();
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 )
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
        writeArray( signature, buf );
        buf.writeBoolean( signedPreview );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 )
        {
            chain.write( buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
