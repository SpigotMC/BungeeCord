package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.util.Either;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ServerLinks extends DefinedPacket
{

    private Link[] links;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        int len = readVarInt( buf );
        links = new Link[ len ];
        for ( int i = 0; i < len; i++ )
        {
            Either<Integer, BaseComponent> type;
            if ( buf.readBoolean() )
            {
                type = Either.left( readVarInt( buf ) );
            } else
            {
                type = Either.right( readBaseComponent( buf, protocolVersion ) );
            }
            String url = readString( buf );

            links[i] = new Link( type, url );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeVarInt( links.length, buf );
        for ( Link link : links )
        {
            Either<Integer, BaseComponent> type = link.getType();
            if ( type.isLeft() )
            {
                buf.writeBoolean( true );
                writeVarInt( type.getLeft(), buf );
            } else
            {
                buf.writeBoolean( false );
                writeBaseComponent( type.getRight(), buf, protocolVersion );
            }
            writeString( link.getUrl(), buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Data
    public static class Link
    {

        private final Either<Integer, BaseComponent> type;
        private final String url;
    }
}
