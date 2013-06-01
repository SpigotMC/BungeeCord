package net.md_5.bungee.protocol.packet.forge;

import net.md_5.bungee.protocol.packet.*;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class Forge1Login extends Packet1Login
{

    private Forge1Login()
    {
        super();
    }

    public Forge1Login(int entityId, String levelType, byte gameMode, int dimension, byte difficulty, byte unused, byte maxPlayers)
    {
        super( entityId, levelType, gameMode, dimension, difficulty, unused, maxPlayers );
    }

    @Override
    public void read(ByteBuf buf)
    {
        entityId = buf.readInt();
        levelType = readString( buf );
        gameMode = buf.readByte();
        dimension = buf.readInt();
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
        buf.writeInt( dimension );
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
