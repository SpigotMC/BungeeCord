package net.md_5.bungee.protocol.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.AbstractDefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerListHeaderFooter extends AbstractDefinedPacket
{

    private String header;
    private String footer;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        header = readString( buf );
        footer = readString( buf );
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( header, buf );
        writeString( footer, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
