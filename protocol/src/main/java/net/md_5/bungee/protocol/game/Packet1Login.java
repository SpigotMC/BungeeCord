package net.md_5.bungee.protocol.game;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.md_5.bungee.protocol.AbstractPacketHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Packet1Login extends DefinedPacket
{

    private int entityId;
    private String levelType;
    private byte gameMode;
    private int dimension;
    private byte difficulty;
    private byte unused;
    private byte maxPlayers;

    @Override
    public void read(ByteBuf buf)
    {
        entityId = buf.readInt();
        levelType = readString( buf );
        gameMode = buf.readByte();
        dimension = buf.readByte();
        difficulty = buf.readByte();
        unused = buf.readByte();
        maxPlayers = buf.readByte();
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeInt( entityId );
        writeString( levelType, buf );
        buf.writeByte( gameMode );
        buf.writeByte( dimension );
        buf.writeByte( difficulty );
        buf.writeByte( unused );
        buf.writeByte( maxPlayers );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
