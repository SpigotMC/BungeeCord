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
public class SetExp extends DefinedPacket
{

    float expBar;
    int level;
    int totalExp;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeFloat( this.expBar );
        DefinedPacket.writeVarInt( level, buf );
        DefinedPacket.writeVarInt( totalExp, buf );
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        this.expBar = buf.readFloat();
        this.level = DefinedPacket.readVarInt( buf );
        this.totalExp = DefinedPacket.readVarInt( buf );
    }

    public SetExp reset()
    {
        this.expBar = 0;
        this.level = 0;
        this.totalExp = 0;
        return this;
    }

    public SetExp increase()
    {
        this.expBar = expBar + 0.01695f < 1 ? expBar + 0.01695f : 1;
        this.level++;
        this.totalExp++;
        return this;
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }
}
