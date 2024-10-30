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

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ServerData extends DefinedPacket
{

    private BaseComponent motd;
    private Object icon;
    private boolean preview;
    private boolean enforceSecure;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_4 || buf.readBoolean() )
        {
            motd = readBaseComponent( buf, protocolVersion );
        }
        if ( buf.readBoolean() )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_4 )
            {
                icon = readArray( buf );
            } else
            {
                icon = readString( buf );
            }
        }

        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_19_3 )
        {
            preview = buf.readBoolean();
        }

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 && protocolVersion < ProtocolConstants.MINECRAFT_1_20_5 )
        {
            enforceSecure = buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( motd != null )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_4 )
            {
                buf.writeBoolean( true );
            }
            writeBaseComponent( motd, buf, protocolVersion );
        } else
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_4 )
            {
                throw new IllegalArgumentException( "MOTD required for this version" );
            }

            buf.writeBoolean( false );
        }

        if ( icon != null )
        {
            buf.writeBoolean( true );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_4 )
            {
                writeArray( (byte[]) icon, buf );
            } else
            {
                writeString( (String) icon, buf );
            }
        } else
        {
            buf.writeBoolean( false );
        }

        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_19_3 )
        {
            buf.writeBoolean( preview );
        }

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 && protocolVersion < ProtocolConstants.MINECRAFT_1_20_5 )
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
