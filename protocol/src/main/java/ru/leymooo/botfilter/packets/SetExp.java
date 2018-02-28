package ru.leymooo.botfilter.packets;

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
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }
}
