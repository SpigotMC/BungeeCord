package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class Packet10HeldItem extends DefinedPacket {

    public short slot;

    public Packet10HeldItem(short slot) {
        super(0x10);
        writeShort(slot);
    }

    public Packet10HeldItem(byte[] buf) {
        super(0x10, buf);
        this.slot = readShort();
    }
}
