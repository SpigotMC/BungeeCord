package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.UserConnection;
import net.md_5.mendax.protocols.PacketDefinitions;
import net.md_5.mendax.protocols.VanillaPacketDefinitions;

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

	protected Packet1Login(int id)
	{
		super(id);
	}

	protected Packet1Login(int id, byte[] buf)
	{
		super(id, buf);
	}

    public Packet1Login(int entityId, String levelType, byte gameMode, byte dimension, byte difficulty, byte unused, byte maxPlayers)
    {
        super(0x01);
        writeInt(entityId);
        writeUTF(levelType);
        writeByte(gameMode);
        writeByte(dimension);
        writeByte(difficulty);
        writeByte(unused);
        writeByte(maxPlayers);
    }

	public Packet1Login getPacketForProtocol(PacketDefinitions packetDefinitions) {
		if(packetDefinitions instanceof VanillaPacketDefinitions) {
			return this;
		} else {
			return new Packet1LoginForge(entityId, levelType, gameMode, dimension, difficulty, unused, maxPlayers);
		}
	}

    public Packet1Login(byte[] buf)
    {
        super(0x01, buf);
        this.entityId = readInt();
        this.levelType = readUTF();
        this.gameMode = readByte();
        this.dimension = readByte();
        this.difficulty = readByte();
        this.unused = readByte();
        this.maxPlayers = readByte();
    }
}
