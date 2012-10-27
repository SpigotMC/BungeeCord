package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketC9PlayerListItem extends DefinedPacket {
    public PacketC9PlayerListItem(String username, boolean online, int ping) {
        super(0xC9);
        writeUTF(username);
        writeBoolean(online);
        writeShort(ping);
    }
}
