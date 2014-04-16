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
public class KeepAlive extends DefinedPacket
{

    private int randomId;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( direction == ProtocolConstants.Direction.TO_SERVER && protocolVersion >= ProtocolConstants.MINECRAFT_14_11_a )
        {
            randomId = readVarInt( buf );
        } else
        {
            randomId = buf.readInt();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( direction == ProtocolConstants.Direction.TO_SERVER && protocolVersion >= ProtocolConstants.MINECRAFT_14_11_a )
        {
            writeVarInt( randomId, buf );
        } else
        {
            buf.writeInt( randomId );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
