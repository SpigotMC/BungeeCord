package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ConfirmTransaction extends DefinedPacket
{

    byte window;
    short action;
    boolean accepted;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int version)
    {
        window = buf.readByte();
        action = buf.readShort();
        accepted = buf.readBoolean();
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version)
    {
        buf.writeByte( window );
        buf.writeShort( action );
        buf.writeBoolean( accepted );
    }

    @Override
    public void handle(AbstractPacketHandler paramAbstractPacketHandler) throws Exception
    {
        paramAbstractPacketHandler.handle( this );
    }
}
