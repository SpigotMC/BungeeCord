package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

public class TeleportConfirm extends DefinedPacket{
    int TeleportId = 0;
    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        if (protocolVersion != 47) {
            TeleportId = DefinedPacket.readVarInt( buf );
        }
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof TeleportConfirm) {
            return true;
        }
        return false;
    }

    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
    public int getTeleportId() {
        return TeleportId;
    }
    @Override
    public int hashCode() {
        return 59*TeleportId;
    }

    @Override
    public String toString() {
        return "TeleportConfirm(TeleportId=" + TeleportId+")";
    }
    
}
