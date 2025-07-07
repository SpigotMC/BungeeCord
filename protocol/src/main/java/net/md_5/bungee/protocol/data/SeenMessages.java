package net.md_5.bungee.protocol.data;

import io.netty.buffer.ByteBuf;
import java.util.BitSet;
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
public class SeenMessages extends DefinedPacket
{

    private int offset;
    private BitSet acknowledged;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        offset = DefinedPacket.readVarInt( buf );
        acknowledged = DefinedPacket.readFixedBitSet( 20, buf );
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        DefinedPacket.writeVarInt( offset, buf );
        DefinedPacket.writeFixedBitSet( acknowledged, 20, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        throw new UnsupportedOperationException( "Not supported." );
    }
}
