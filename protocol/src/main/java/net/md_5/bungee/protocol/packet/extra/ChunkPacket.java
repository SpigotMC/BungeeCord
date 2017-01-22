package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

public class ChunkPacket extends DefinedPacket {

    int x;
    int z;
    byte[] data;

    public ChunkPacket(int x, int z) {
        this.x = x;
        this.z = z;
        this.data = new byte[256];
        Arrays.fill(this.data, (byte) 18);
    }

    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version) {
        buf.writeInt(this.x);
        buf.writeInt(this.z);
        buf.writeBoolean(true);
        DefinedPacket.writeVarInt(0, buf);
        DefinedPacket.writeVarInt(256, buf);
        buf.writeBytes(this.data);
        if (version >= 110) {
            DefinedPacket.writeVarInt(0, buf);
        }
    }

    public void handle(AbstractPacketHandler handler) throws Exception {}

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String toString() {
        return "ChunkPacket(x=" + this.getX() + ", z=" + this.getZ() + ", data=" + Arrays.toString(this.getData()) + ")";
    }

    @ConstructorProperties({ "x", "z", "data"})
    public ChunkPacket(int x, int z, byte[] data) {
        this.x = x;
        this.z = z;
        this.data = data;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ChunkPacket)) {
            return false;
        } else {
            ChunkPacket other = (ChunkPacket) o;

            return !other.canEqual(this) ? false : (this.getX() != other.getX() ? false : (this.getZ() != other.getZ() ? false : Arrays.equals(this.getData(), other.getData())));
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof ChunkPacket;
    }

    public int hashCode() {
        byte result = 1;
        int result1 = result * 59 + this.getX();

        result1 = result1 * 59 + this.getZ();
        result1 = result1 * 59 + Arrays.hashCode(this.getData());
        return result1;
    }
}
