package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ChatSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.util.Deserializable;
import net.md_5.bungee.protocol.util.Either;
import net.md_5.bungee.protocol.util.FunctionDeserializable;
import net.md_5.bungee.protocol.util.NoOrigDeserializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Kick extends DefinedPacket
{

    private Deserializable<Either<String, TypedTag>, BaseComponent> messageRaw;

    @Override
    public void read(ByteBuf buf, Protocol protocol, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocol == Protocol.LOGIN )
        {
            String json = readString( buf );
            messageRaw = new FunctionDeserializable<>( Either.left( json ), (ov) -> ChatSerializer.forVersion( protocolVersion ).deserialize( ov.getLeft() ) );
        } else
        {
            messageRaw = readBaseComponent( buf, protocolVersion );
        }
    }

    @Override
    public void write(ByteBuf buf, Protocol protocol, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocol == Protocol.LOGIN )
        {
            writeString( ChatSerializer.forVersion( protocolVersion ).toString( messageRaw.get() ), buf );
        } else
        {
            writeBaseComponent( messageRaw, buf, protocolVersion );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public Kick(BaseComponent message)
    {
        setMessage( message );
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
