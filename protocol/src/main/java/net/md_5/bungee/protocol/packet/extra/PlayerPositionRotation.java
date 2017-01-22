package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import java.beans.ConstructorProperties;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

public class PlayerPositionRotation
extends DefinedPacket {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private int teleportId;
    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeByte(0);
        if (protocolVersion >= 107) {
            PlayerPositionRotation.writeVarInt(teleportId, buf);
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        throw new UnsupportedOperationException();
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
    public int getTeleportId() {
        return this.teleportId;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public String toString() {
        return "PlayerPositionRotation(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ", yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
    }

    @ConstructorProperties(value={"x", "y", "z", "yaw", "pitch", "teleportId"})
    public PlayerPositionRotation(double x, double y, double z, float yaw, float pitch, int teleportId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.teleportId = teleportId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PlayerPositionRotation)) {
            return false;
        }
        PlayerPositionRotation other = (PlayerPositionRotation)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (Double.compare(this.getX(), other.getX()) != 0) {
            return false;
        }
        if (Double.compare(this.getY(), other.getY()) != 0) {
            return false;
        }
        if (Double.compare(this.getZ(), other.getZ()) != 0) {
            return false;
        }
        if (Float.compare(this.getYaw(), other.getYaw()) != 0) {
            return false;
        }
        if (Float.compare(this.getPitch(), other.getPitch()) != 0) {
            return false;
        }
        if (Float.compare(this.getTeleportId(), other.getTeleportId()) != 0) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(Object other) {
        return other instanceof PlayerPositionRotation;
    }

    @Override
    public int hashCode() {
        int result = 1;
        long a = Double.doubleToLongBits(this.getX());
        result = result * 59 + (int)(a >>> 32 ^ a);
        long b = Double.doubleToLongBits(this.getY());
        result = result * 59 + (int)(b >>> 32 ^ b);
        long c = Double.doubleToLongBits(this.getZ());
        result = result * 59 + (int)(c >>> 32 ^ c);
        result = result * 59 + Float.floatToIntBits(this.getYaw());
        result = result * 59 + Float.floatToIntBits(this.getPitch());
        result = result * 59 + teleportId;
        return result;
    }
}
