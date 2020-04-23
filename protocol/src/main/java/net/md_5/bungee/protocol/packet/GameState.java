package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GameState extends DefinedPacket<GameState>
{

    public static final short IMMEDIATE_RESPAWN = 11;
    //
    private short state;
    private float value;

    @Override
    public void read(ByteBuf buf)
    {
        state = buf.readUnsignedByte();
        value = buf.readFloat();
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeByte( state );
        buf.writeFloat( value );
    }

    @Override
    public void callHandler(AbstractPacketHandler handler, PacketWrapper<GameState> packet) throws Exception
    {
        handler.handleGameState( packet );
    }
}
