package net.md_5.bungee.protocol.packet;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ConfirmTransaction extends DefinedPacket{
    
    Byte window;
    Short action;
    Boolean accpted;
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int version) {
        window = buf.readByte();
        action = buf.readShort();
        accpted = buf.readBoolean();
    }
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version) {
        buf.writeByte(window);
        buf.writeShort(action);
        buf.writeBoolean(accpted);
    }
    @Override
    public void handle(AbstractPacketHandler paramAbstractPacketHandler) throws Exception {
        paramAbstractPacketHandler.handle(this);
    }
    public byte getWindow() {
        return window;
    }
    public short getAction() {
        return action;
    }
    public boolean isAccepted() {
        return accpted;
    }

    @Override
    public String toString() {
        return "ConfirmTransaction(windowId=" + this.getWindow() + ", action=" + this.getAction() + ", accepted=" + isAccepted() + ")";
    }
    public ConfirmTransaction(Byte window, Short action, Boolean accepted) {
        this.window = window;
        this.action = action;
        this.accpted = accepted;
    }
}
