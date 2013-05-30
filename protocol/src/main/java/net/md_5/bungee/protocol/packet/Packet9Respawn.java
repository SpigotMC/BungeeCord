package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class Packet9Respawn extends DefinedPacket
{

    private int dimension;
    private byte difficulty;
    private byte gameMode;
    private short worldHeight;
    private String levelType;

    Packet9Respawn()
    {
        super( 0x09 );
    }

    @Override
    public void read(ByteBuf buf)
    {
        dimension = buf.readInt();
        difficulty = buf.readByte();
        gameMode = buf.readByte();
        worldHeight = buf.readShort();
        levelType = readString( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeInt( dimension );
        buf.writeByte( difficulty );
        buf.writeByte( gameMode );
        buf.writeShort( worldHeight );
        writeString( levelType, buf );
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
