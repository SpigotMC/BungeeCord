package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
public class PlayerListHeaderFooter extends DefinedPacket
{

    private Deserializable<Either<String, TypedTag>, BaseComponent> headerRaw;
    private Deserializable<Either<String, TypedTag>, BaseComponent> footerRaw;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        headerRaw = readBaseComponent( buf, protocolVersion );
        footerRaw = readBaseComponent( buf, protocolVersion );
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeBaseComponent( headerRaw, buf, protocolVersion );
        writeBaseComponent( footerRaw, buf, protocolVersion );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public PlayerListHeaderFooter(BaseComponent header, BaseComponent footer)
    {
        setHeader( header );
        setFooter( footer );
    }

    public BaseComponent getHeader()
    {
        if ( headerRaw == null )
        {
            return null;
        }
        return headerRaw.get();
    }

    public void setHeader(BaseComponent header)
    {
        if ( header == null )
        {
            this.headerRaw = null;
            return;
        }
        this.headerRaw = new NoOrigDeserializable<>( header );
    }

    public BaseComponent getFooter()
    {
        if ( footerRaw == null )
        {
            return null;
        }
        return footerRaw.get();
    }

    public void setFooter(BaseComponent footer)
    {
        if ( footer == null )
        {
            this.footerRaw = null;
            return;
        }
        this.footerRaw = new NoOrigDeserializable<>( footer );
    }
}
