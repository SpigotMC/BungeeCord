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
public class ServerData extends DefinedPacket
{

    private String motd;
    private String icon;
    private boolean preview;
    private boolean enforceSecure;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( buf.readBoolean() )
        {
            motd = readString( buf, 262144 );
        }
        if ( buf.readBoolean() )
        {
            icon = readString( buf );
        }

        preview = buf.readBoolean();

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 )
        {
            enforceSecure = buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( motd != null )
        {
            buf.writeBoolean( true );
            writeString( motd, buf, 262144 );
        } else
        {
            buf.writeBoolean( false );
        }

        if ( icon != null )
        {
            buf.writeBoolean( true );
            writeString( icon, buf );
        } else
        {
            buf.writeBoolean( false );
        }

        buf.writeBoolean( preview );

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 )
        {
            buf.writeBoolean( enforceSecure );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
