package net.md_5.bungee.packet;

import net.md_5.mendax.protocols.ForgePacketDefinitions;
import net.md_5.mendax.protocols.PacketDefinitions;

public class Packet1LoginForge extends Packet1Login
{
    public Packet1LoginForge(int entityId, String levelType, byte gameMode, int dimension, byte difficulty, byte unused, byte maxPlayers)
    {
        super(0x01);
        writeInt(entityId);
        writeUTF(levelType);
        writeByte(gameMode);
        writeInt(dimension);
        writeByte(difficulty);
        writeByte(unused);
        writeByte(maxPlayers);
    }

	@Override
	public Packet1Login getPacketForProtocol(PacketDefinitions packetDefinitions) {
		if(packetDefinitions instanceof ForgePacketDefinitions) {
			return this;
		} else {
			return new Packet1Login(entityId, levelType, gameMode, (byte)dimension, difficulty, unused, maxPlayers);
		}
	}

    public Packet1LoginForge(byte[] buf)
    {
        super(0x01, buf);
        this.entityId = readInt();
        this.levelType = readUTF();
        this.gameMode = readByte();
        this.dimension = readInt();
        this.difficulty = readByte();
        this.unused = readByte();
        this.maxPlayers = readByte();
    }
}
