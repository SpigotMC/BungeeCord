package net.md_5.bungee.protocol.packet.extra;

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
public class Animation extends DefinedPacket
{

    private int entId = -1;
    private int animId = 0;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( direction == ProtocolConstants.Direction.TO_CLIENT )
        {
            buf.skipBytes( buf.readableBytes() );
        } else if ( protocolVersion != 47 ) //no packet data
        {
            animId = Animation.readVarInt( buf );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( direction == ProtocolConstants.Direction.TO_CLIENT )
        {
            Animation.writeVarInt( entId, buf );
            buf.writeByte( animId );
        } else if ( protocolVersion != 47 ) //1.8 empty data
        {
            Animation.writeVarInt( animId, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

}
