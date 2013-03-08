package net.md_5.bungee.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class Packet1Login extends DefinedPacket
{

    public int entityId;
    public String levelType;
    public byte gameMode;
    public int dimension;
    public byte difficulty;
    public byte unused;
    public byte maxPlayers;

    public Packet1Login(int entityId, String levelType, byte gameMode, byte dimension, byte difficulty, byte unused, byte maxPlayers)
    {
        super( 0x01 );
        writeInt( entityId );
        writeString( levelType );
        writeByte( gameMode );
        writeByte( dimension );
        writeByte( difficulty );
        writeByte( unused );
        writeByte( maxPlayers );
    }

    public Packet1Login(ByteBuf buf)
    {
        super( 0x01, buf );
        this.entityId = readInt();
        this.levelType = readString();
        this.gameMode = readByte();
        if ( readableBytes() == 4 )
        {
            this.dimension = readByte();
        } else if ( readableBytes() == 7 )
        {
            this.dimension = readInt();
        } else
        {
            throw new IllegalStateException();
        }
        this.difficulty = readByte();
        this.unused = readByte();
        this.maxPlayers = readByte();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
