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
public class PlayerAbilities extends DefinedPacket
{

    byte flags;
    float speed;
    float field;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeByte( this.flags );
        buf.writeFloat( this.speed );
        buf.writeFloat( this.field );
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        this.flags = buf.readByte();
        this.speed = buf.readFloat();
        this.field = buf.readFloat();
    }
    
    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }
}
