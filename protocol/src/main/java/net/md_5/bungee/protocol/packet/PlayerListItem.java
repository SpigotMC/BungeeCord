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
public class PlayerListItem extends DefinedPacket
{

    private String username;
    private boolean online;
    private int ping;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        username = readString( buf );
        online = buf.readBoolean();
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_14_11_a )
        {
            ping = readVarInt( buf );
        } else
        {
            ping = buf.readShort();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( username, buf );
        buf.writeBoolean( online );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_14_11_a )
        {
            writeVarInt( ping, buf );
        } else
        {
            buf.writeShort( ping );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
