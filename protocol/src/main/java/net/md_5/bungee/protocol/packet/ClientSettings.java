package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ProtocolConstants;

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
    private byte showCape;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        locale = readString( buf );
        viewDistance = buf.readByte();
        chatFlags = buf.readByte();
        unknown = buf.readBoolean();
        if ( protocolVersion <= ProtocolConstants.MINECRAFT_1_7_6 )
        {
            difficulty = buf.readByte();
        }
        showCape = buf.readByte();
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( locale, buf );
        buf.writeByte( viewDistance );
        buf.writeByte( chatFlags );
        buf.writeBoolean( unknown );
        if ( protocolVersion <= ProtocolConstants.MINECRAFT_1_7_6 )
        {
            buf.writeByte( difficulty );
        }
        buf.writeByte( showCape );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
