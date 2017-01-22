package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import java.beans.ConstructorProperties;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

public class PlayerAbilities
extends DefinedPacket {
    byte flags;
    float speed;
    float field;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.flags);
        buf.writeFloat(this.speed);
        buf.writeFloat(this.field);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
    }

    public byte getFlags() {
        return this.flags;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float getField() {
        return this.field;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setField(float field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "PlayerAbilities(flags=" + this.getFlags() + ", speed=" + this.getSpeed() + ", field=" + this.getField() + ")";
    }

    @ConstructorProperties(value={"flags", "speed", "field"})
    public PlayerAbilities(byte flags, float speed, float field) {
        this.flags = flags;
        this.speed = speed;
        this.field = field;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PlayerAbilities)) {
            return false;
        }
        PlayerAbilities other = (PlayerAbilities)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getFlags() != other.getFlags()) {
            return false;
        }
        if (Float.compare(this.getSpeed(), other.getSpeed()) != 0) {
            return false;
        }
        if (Float.compare(this.getField(), other.getField()) != 0) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(Object other) {
        return other instanceof PlayerAbilities;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 59 + this.getFlags();
        result = result * 59 + Float.floatToIntBits(this.getSpeed());
        result = result * 59 + Float.floatToIntBits(this.getField());
        return result;
    }
}
