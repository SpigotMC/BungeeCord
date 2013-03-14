package net.md_5.bungee.packet;

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
        writeUTF( levelType );
        writeByte( gameMode );
        writeByte( dimension );
        writeByte( difficulty );
        writeByte( unused );
        writeByte( maxPlayers );
        this.entityId = entityId;
        this.levelType = levelType;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.unused = unused;
        this.maxPlayers = maxPlayers;
    }

    Packet1Login(byte[] buf)
    {
        super( 0x01, buf );
        this.entityId = readInt();
        this.levelType = readUTF();
        this.gameMode = readByte();
        if ( available() == 4 )
        {
            this.dimension = readByte();
        } else if ( available() == 7 )
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
