package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import java.beans.ConstructorProperties;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

public class SetSlot
extends DefinedPacket {
    private int windowId;
    private int slot;
    private int item;
    private int data;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        buf.writeByte(this.windowId);
        buf.writeShort(this.slot);
        buf.writeShort(this.item);
        buf.writeByte(1);
        buf.writeShort(this.data);
        buf.writeByte(0);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        throw new UnsupportedOperationException();
    }

    public int getWindowId() {
        return this.windowId;
    }

    public int getSlot() {
        return this.slot;
    }

    public int getItem() {
        return this.item;
    }

    public int getData() {
        return this.data;
    }

    public void setWindowId(int windowId) {
        this.windowId = windowId;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public void setData(int data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SetSlot(windowId=" + this.getWindowId() + ", slot=" + this.getSlot() + ", item=" + this.getItem() + ", data=" + this.getData() + ")";
    }


    @ConstructorProperties(value={"windowId", "slot", "item", "data"})
    public SetSlot(int windowId, int slot, int item, int data) {
        this.windowId = windowId;
        this.slot = slot;
        this.item = item;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SetSlot)) {
            return false;
        }
        SetSlot other = (SetSlot)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getWindowId() != other.getWindowId()) {
            return false;
        }
        if (this.getSlot() != other.getSlot()) {
            return false;
        }
        if (this.getItem() != other.getItem()) {
            return false;
        }
        if (this.getData() != other.getData()) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(Object other) {
        return other instanceof SetSlot;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 59 + this.getWindowId();
        result = result * 59 + this.getSlot();
        result = result * 59 + this.getItem();
        result = result * 59 + this.getData();
        return result;
    }
}
