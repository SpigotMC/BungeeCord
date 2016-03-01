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
    private int chatFlags;
    private boolean chatColours;
    private byte difficulty;
    private byte skinParts;
    private int mainHand;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        locale = readString( buf );
        viewDistance = buf.readByte();
        chatFlags = protocolVersion >= ProtocolConstants.MINECRAFT_1_9 ? DefinedPacket.readVarInt( buf ) : buf.readUnsignedByte();
        chatColours = buf.readBoolean();
        skinParts = buf.readByte();
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
        {
            mainHand = DefinedPacket.readVarInt( buf );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( locale, buf );
        buf.writeByte( viewDistance );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
        {
            DefinedPacket.writeVarInt( chatFlags, buf );
        } else
        {
            buf.writeByte( chatFlags );
        }
        buf.writeBoolean( chatColours );
        buf.writeByte( skinParts );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
        {
            DefinedPacket.writeVarInt(mainHand, buf);
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
