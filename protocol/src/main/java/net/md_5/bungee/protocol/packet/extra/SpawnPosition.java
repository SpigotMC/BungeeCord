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
public class SpawnPosition extends DefinedPacket
{

    private int x;
    private int y;
    private int z;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeLong( ( this.x & 67108863L ) << 38L | ( this.y & 4095L ) << 26L | ( this.z & 67108863L ) << 0 );
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.skipBytes( buf.readableBytes() );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }

}
