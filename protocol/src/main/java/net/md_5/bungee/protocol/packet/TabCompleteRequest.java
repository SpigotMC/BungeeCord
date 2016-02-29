package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TabCompleteRequest extends DefinedPacket
{

    private String cursor;
    private boolean assumeCommand;
    private boolean hasPositon;
    private long position;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        cursor = readString( buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
        {
            assumeCommand = buf.readBoolean();
        }

        if ( hasPositon = buf.readBoolean() )
        {
            position = buf.readLong();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( cursor, buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
        {
            buf.writeBoolean( assumeCommand );
        }

        buf.writeBoolean( hasPositon );
        if ( hasPositon )
        {
            buf.writeLong( position );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
