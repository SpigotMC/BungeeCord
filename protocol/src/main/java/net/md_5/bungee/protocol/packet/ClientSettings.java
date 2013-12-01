package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientSettings extends DefinedPacket
{

    private String locale;
    private byte viewDistance;
    private byte chatFlags;
    private boolean unknown;
    private byte difficulty;
    private boolean showCape;

    @Override
    public void read(ByteBuf buf)
    {
        locale = readString( buf );
        viewDistance = buf.readByte();
        chatFlags = buf.readByte();
        unknown = buf.readBoolean();
        difficulty = buf.readByte();
        showCape = buf.readBoolean();
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString( locale, buf );
        buf.writeByte( viewDistance );
        buf.writeByte( chatFlags );
        buf.writeBoolean( unknown );
        buf.writeByte( difficulty );
        buf.writeBoolean( showCape );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
