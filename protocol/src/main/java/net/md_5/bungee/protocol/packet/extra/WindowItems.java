package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import java.beans.ConstructorProperties;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

public class WindowItems
extends DefinedPacket {
    private int windowId;
    private int slot;
    private int item;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        buf.writeByte(this.windowId);
        buf.writeShort(this.slot + 1);
        for (int i = 0; i < this.slot; ++i) {
            buf.writeShort(-1);
        }
        buf.writeShort(this.item);
        buf.writeByte(1);
        buf.writeShort(15);
        buf.writeByte(0);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
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

    public void setWindowId(int windowId) {
        this.windowId = windowId;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setItem(int item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "WindowItems(windowId=" + this.getWindowId() + ", slot=" + this.getSlot() + ", item=" + this.getItem() + ")";
    }

    @ConstructorProperties(value={"windowId", "slot", "item"})
    public WindowItems(int windowId, int slot, int item) {
        this.windowId = windowId;
        this.slot = slot;
        this.item = item;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WindowItems)) {
            return false;
        }
        WindowItems other = (WindowItems)o;
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
        return true;
    }

    protected boolean canEqual(Object other) {
        return other instanceof WindowItems;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 59 + this.getWindowId();
        result = result * 59 + this.getSlot();
        result = result * 59 + this.getItem();
        return result;
    }
}
