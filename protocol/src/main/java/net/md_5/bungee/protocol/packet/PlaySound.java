package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

/**
 * Packet to Play Sound at the player
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlaySound extends DefinedPacket{

    private String soundName;
    private int posX;
    private int posY;
    private int posZ;
    private float volume;
    private int pitch;

    @Override
    public void read(ByteBuf buf){
        soundName=readString(buf);
        posX=readVarInt(buf);
        posY=readVarInt(buf);
        posZ=readVarInt(buf);
        volume=readVarInt(buf);
        pitch=buf.readUnsignedByte();
    }

    @Override
    public void write(ByteBuf buf){
        writeString(soundName, buf);
        writeVarInt(posX, buf);
        writeVarInt(posY, buf);
        writeVarInt(posZ, buf);
        buf.writeFloat(volume);
        buf.writeByte(pitch);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception{
        handler.handle(this);
    }
}
