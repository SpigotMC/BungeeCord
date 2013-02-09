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
    public byte dimension;
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
    }

    public Packet1Login(byte[] buf)
    {
        super( 0x01, buf );
        this.entityId = readInt();
        this.levelType = readUTF();
        this.gameMode = readByte();
        this.dimension = readByte();
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
