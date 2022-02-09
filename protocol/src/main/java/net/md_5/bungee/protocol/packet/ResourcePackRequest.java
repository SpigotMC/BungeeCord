package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResourcePackRequest extends DefinedPacket
{

    private String url;
    private String hash;
    private boolean forced;
    private BaseComponent[] promptMessage;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        url = readString( buf );
        hash = ( protocolVersion >= ProtocolConstants.MINECRAFT_1_11 ) ? readString( buf, 40 ) : readString( buf );

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_17 )
        {
            forced = buf.readBoolean();
            if ( buf.readBoolean() )
            {
                promptMessage = ComponentSerializer.parse( readString( buf ) );
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( url, buf );
        writeString( hash, buf );

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_17 )
        {
            buf.writeBoolean( forced );
            buf.writeBoolean( promptMessage != null );
            if ( promptMessage != null )
            {
                writeString( ComponentSerializer.toString( promptMessage ), buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
