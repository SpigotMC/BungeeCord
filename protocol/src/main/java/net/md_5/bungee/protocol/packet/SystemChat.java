package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.util.Deserializable;
import net.md_5.bungee.protocol.util.Either;
import net.md_5.bungee.protocol.util.NoOrigDeserializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SystemChat extends DefinedPacket
{

    private Deserializable<Either<String, TypedTag>, BaseComponent> messageRaw;
    private int position;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        messageRaw = readBaseComponent( buf, 262144, protocolVersion );
        position = ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 ) ? ( ( buf.readBoolean() ) ? ChatMessageType.ACTION_BAR.ordinal() : 0 ) : readVarInt( buf );
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeBaseComponent( messageRaw, buf, protocolVersion );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 )
        {
            buf.writeBoolean( position == ChatMessageType.ACTION_BAR.ordinal() );
        } else
        {
            writeVarInt( position, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public SystemChat(BaseComponent message, int position)
    {
        this.messageRaw = new NoOrigDeserializable<>( message );
        this.position = position;
    }

    public BaseComponent getMessage()
    {
        if ( messageRaw == null )
        {
            return null;
        }
        return messageRaw.get();
    }

    public void setMessage(BaseComponent message)
    {
        if ( message == null )
        {
            this.messageRaw = null;
            return;
        }
        this.messageRaw = new NoOrigDeserializable<>( message );
    }

}
