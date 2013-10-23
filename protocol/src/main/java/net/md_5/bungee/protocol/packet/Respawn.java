package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Respawn extends DefinedPacket
{

    private int dimension;
    private short difficulty;
    private short gameMode;
    private String levelType;

    @Override
    public void read(ByteBuf buf)
    {
        dimension = buf.readInt();
        difficulty = buf.readUnsignedByte();
        gameMode = buf.readUnsignedByte();
        levelType = readString( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeInt( dimension );
        buf.writeByte( difficulty );
        buf.writeByte( gameMode );
        writeString( levelType, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
